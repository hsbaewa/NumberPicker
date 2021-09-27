package kr.hs.numberpicker

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kr.hs.numberpicker.databinding.ItemNumberPickerBinding

class NumberPickerRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var numberPickerSnapHelper: NumberPickerSnapHelper? = null
    private var snapPosition = NO_POSITION
    private var snapHolder: NumberHolder? = null

    var numberFormat: String? = null
    var numberFontSizeDP: Float? = null
    var snapTextColor: Int? = null
    var defaultTextColor: Int? = null

    var onSnapListener: OnSnapNumberListener? = null

    fun setup(start: Int, count: Int, defaultValue: Int) {
        adapter = NumberPickerAdapter(start, count)

        val layoutManager = LinearLayoutManager(context)
        this.layoutManager = layoutManager

        // 리스트를 특정 아이템에 물리도록 해주는 설정
        numberPickerSnapHelper = NumberPickerSnapHelper()
        numberPickerSnapHelper?.attachToRecyclerView(this)

        post {
            // 스크롤 포지션을 가운데로 해주는 부분으로 그냥 scrollToPosition 함수를 사용하면 특정 포지션이 리스트 상단에 위치하는데 iOS TimePicker처럼 가운데로 하도록 하기 위하여 아래처럼 수정함.
            val height = layoutParams.height
            val itemHeight = getChildAt(0).height
            val centerValue = ((Integer.MAX_VALUE / 2) % count) + start

            layoutManager.scrollToPositionWithOffset(
                ((Integer.MAX_VALUE / 2) - centerValue) + defaultValue,
                (height / 2) - (itemHeight / 2)
            )
        }
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        numberPickerSnapHelper?.let { snapHelper ->
            val snapPosition = snapHelper.getSnapPosition()
            val snapPositionChanged = this.snapPosition != snapPosition

            if (snapPositionChanged) {
                snapHolder?.binding?.textColorRes = defaultTextColor

                val holder = findViewHolderForAdapterPosition(snapPosition) as? NumberHolder
                holder?.binding?.textColorRes = snapTextColor
                snapHolder = holder
                this.snapPosition = snapPosition
                this.onSnapListener?.onSnap(this, holder?.binding?.value ?: -1)
            }
        }
    }

    inner class NumberPickerAdapter(val start: Int, val end: Int) :
        RecyclerView.Adapter<NumberHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemNumberPickerBinding.inflate(inflater, parent, false)
            return NumberHolder(binding)
        }

        override fun onBindViewHolder(holder: NumberHolder, position: Int) {
            val binding = holder.binding
            val value = (position % end) + start
            binding.value = value
            binding.textSize = this@NumberPickerRecyclerView.numberFontSizeDP
            binding.textColorRes = this@NumberPickerRecyclerView.defaultTextColor

            binding.tvValue.text = this@NumberPickerRecyclerView.numberFormat?.let {
                String.format(it, value)
            } ?: let {
                value.toString()
            }
        }

        override fun getItemCount(): Int {
            return Integer.MAX_VALUE
        }
    }

    inner class NumberHolder(val binding: ItemNumberPickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class NumberPickerSnapHelper : LinearSnapHelper() {
        fun getSnapPosition(): Int {
            val layoutManager = layoutManager ?: return NO_POSITION
            val snapView = findSnapView(layoutManager) ?: return NO_POSITION
            return layoutManager.getPosition(snapView)
        }
    }

    interface OnSnapNumberListener {
        fun onSnap(recyclerView: RecyclerView, number: Int)
    }


    companion object {
        @JvmStatic
        @BindingAdapter("app:textSize")
        fun textSize(textView: TextView, size: Float?) {
            size?.let {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, it)
            }
        }
    }
}