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

        binding.picker1.setOnSnapListener(this)
        binding.picker2.setOnSnapListener(this)
        binding.picker3.setOnSnapListener(this)

        binding.btnUp.setOnClickListener {
            binding.picker1.up()
            binding.picker2.up()
            binding.picker3.up()
        }

        binding.btnDown.setOnClickListener {
            binding.picker1.down()
            binding.picker2.down()
            binding.picker3.down()
        }
    }

    override fun onSnap(recyclerView: RecyclerView, number: Int) {
        when (recyclerView) {
            binding.picker1 -> binding.textView1.text = "선택된 값 : $number"
            binding.picker2 -> binding.textView2.text = "선택된 값 : $number"
            binding.picker3 -> binding.textView3.text = "선택된 값 : $number"
        }
    }
}