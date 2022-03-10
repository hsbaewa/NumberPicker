package kr.hs.numberpicker

import android.content.Context
import android.graphics.Color
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
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttr(attrs)
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttr(attrs)
        setup()
    }

    private var numberPickerSnapHelper: NumberPickerSnapHelper? = null
    private var snapPosition = NO_POSITION
    private var snapHolder: NumberHolder? = null

    private var numberFormat: String? = null

    private var numberFontSizeDP: Float? = null
    private var snapTextColor: Int? = null
    private var defaultTextColor: Int? = null

    private var start = 0
    private var count = 0
    private var default = 0
    private var interval = 1

    private var onSnapListener: OnSnapNumberListener? = null

    private fun initAttr(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker)
        try {
            val black = Color.BLACK
            defaultTextColor =
                typedArray.getColor(R.styleable.NumberPicker_color, black)
            snapTextColor = typedArray.getColor(R.styleable.NumberPicker_snapColor, black)
            numberFormat = typedArray.getString(R.styleable.NumberPicker_format)
            numberFontSizeDP = typedArray.getDimension(R.styleable.NumberPicker_fontSize, 10f)
            start = typedArray.getInt(R.styleable.NumberPicker_start, 1)
            count = typedArray.getInt(R.styleable.NumberPicker_count, 10)
            default = typedArray.getInt(R.styleable.NumberPicker_defaultValue, 1)
            interval = typedArray.getInt(R.styleable.NumberPicker_interval, 1)
        } finally {
            typedArray.recycle()
        }
    }

    fun setOnSnapListener(onSnapListener: OnSnapNumberListener) {
        this.onSnapListener = onSnapListener
    }

    private fun setup() {
        adapter = NumberPickerAdapter()

        val layoutManager = NumberPickerLayoutManager(context)
        this.layoutManager = layoutManager

        // 리스트를 특정 아이템에 물리도록 해주는 설정
        numberPickerSnapHelper = NumberPickerSnapHelper(layoutManager)
        numberPickerSnapHelper?.attachToRecyclerView(this)

        post {
            // 스크롤 포지션을 가운데로 해주는 부분으로 그냥 scrollToPosition 함수를 사용하면 특정 포지션이 리스트 상단에 위치하는데 iOS TimePicker처럼 가운데로 하도록 하기 위하여 아래처럼 수정함.
            val height = measuredHeight
            val itemHeight = getChildAt(0).measuredHeight
            val centerValue = ((Integer.MAX_VALUE / 2) % count) + start

            layoutManager.scrollToPositionWithOffset(
                ((Integer.MAX_VALUE / 2) - centerValue) + default,
                (height / 2) - (itemHeight / 2)
            )

            postDelayed({
                smoothScrollToPosition(((Integer.MAX_VALUE / 2) - centerValue) + default)
            }, 100)
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

    fun positionToValue(position: Int): Int {
        return ((position % this.count) + this.start) * this.interval
    }

    private inner class NumberPickerAdapter : RecyclerView.Adapter<NumberHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemNumberPickerBinding.inflate(inflater, parent, false)
            return NumberHolder(binding)
        }

        override fun onBindViewHolder(holder: NumberHolder, position: Int) {
            val binding = holder.binding
            val value = positionToValue(position)
            binding.value = value
            binding.textSize = numberFontSizeDP
            binding.textColorRes = defaultTextColor
            binding.tvValue.text = numberFormat?.let {
                String.format(it, value)
            } ?: let {
                value.toString()
            }
        }

        override fun getItemCount(): Int {
            return Integer.MAX_VALUE
        }
    }


    class NumberHolder(val binding: ItemNumberPickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    class NumberPickerSnapHelper(val layoutManager: LinearLayoutManager) : LinearSnapHelper() {
        fun getSnapPosition(): Int {
            val layoutManager = layoutManager
            val snapView = findSnapView(layoutManager) ?: return NO_POSITION
            return layoutManager.getPosition(snapView)
        }

        override fun findTargetSnapPosition(
            layoutManager: LayoutManager?,
            velocityX: Int,
            velocityY: Int
        ): Int {
            return super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        }
    }

    interface OnSnapNumberListener {
        fun onSnap(recyclerView: RecyclerView, number: Int)
    }

    fun setValue(value: Int) {
        if (value == getValue())
            return

        post {
            // 스크롤 포지션을 가운데로 해주는 부분으로 그냥 scrollToPosition 함수를 사용하면 특정 포지션이 리스트 상단에 위치하는데 iOS TimePicker처럼 가운데로 하도록 하기 위하여 아래처럼 수정함.
            val height = measuredHeight
            val itemHeight = getChildAt(0).measuredHeight
            val centerValue = positionToValue(Integer.MAX_VALUE / 2)

            (layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                ((Integer.MAX_VALUE / 2) - (centerValue / interval)) + (value / interval),
                (height / 2) - (itemHeight / 2)
            )

            postDelayed({
                smoothScrollToPosition(
                    ((Integer.MAX_VALUE / 2) - (centerValue / interval)) + (value / interval)
                )
            }, 100)
        }
    }

    fun up() {
        if (!isEnabledScroll())
            return

        val position = numberPickerSnapHelper?.getSnapPosition() ?: 0
        post {
            smoothScrollToPosition(position + 1)
        }
    }

    fun down() {
        if (!isEnabledScroll())
            return

        val position = numberPickerSnapHelper?.getSnapPosition() ?: 0
        post {
            smoothScrollToPosition(position - 1)
        }
    }

    fun getValue(): Int {
        val position = numberPickerSnapHelper?.getSnapPosition() ?: return 0
        if (position < 0)
            return 0
        return positionToValue(position)
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

    private class NumberPickerLayoutManager(context: Context?) : LinearLayoutManager(context) {
        private var isScrollEnabled = true

        fun setScrollEnabled(enable: Boolean) {
            this.isScrollEnabled = enable
        }

        fun isScrollEnabled(): Boolean {
            return this.isScrollEnabled
        }

        override fun canScrollVertically(): Boolean {
            return this.isScrollEnabled && super.canScrollVertically()
        }
    }

    fun disableScroll() {
        (layoutManager as NumberPickerLayoutManager).setScrollEnabled(false)
    }

    fun enableScroll() {
        (layoutManager as NumberPickerLayoutManager).setScrollEnabled(true)
    }

    private fun isEnabledScroll(): Boolean {
        return (layoutManager as NumberPickerLayoutManager).isScrollEnabled()
    }
}