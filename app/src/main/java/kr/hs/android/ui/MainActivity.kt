package kr.hs.android.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.hs.android.R
import kr.hs.android.databinding.ActivityMainBinding
import kr.hs.numberpicker.NumberPickerRecyclerView

class MainActivity : AppCompatActivity(), NumberPickerRecyclerView.OnSnapNumberListener {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val numberPickerRecyclerView = binding.numberPickerRecyclerView

        // 숫자 색상
        numberPickerRecyclerView.defaultTextColor =
            ContextCompat.getColor(this, android.R.color.holo_red_dark)
        // 하이라이팅 된 색상
        numberPickerRecyclerView.snapTextColor =
            ContextCompat.getColor(this, android.R.color.holo_red_light)

        // 숫자 자릿수
        numberPickerRecyclerView.numberFormat = "%02d"
        // 숫자 DP 사이즈
        numberPickerRecyclerView.numberFontSizeDP = 40F
        // 선택된 값을 알기위한 리스너
        numberPickerRecyclerView.onSnapListener = this

        numberPickerRecyclerView.setup(1, 60, 1)
    }

    override fun onSnap(recyclerView: RecyclerView, number: Int) {
        val resultView: TextView = binding.textView
        resultView.text = "선택된 값 : $number"
    }
}