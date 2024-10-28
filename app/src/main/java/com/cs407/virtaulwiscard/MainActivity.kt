package com.cs407.virtualwiscard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum
import com.nightonke.boommenu.BoomButtons.HamButton
import com.nightonke.boommenu.BoomMenuButton
import com.nightonke.boommenu.ButtonEnum
import com.nightonke.boommenu.Piece.PiecePlaceEnum

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val bmb = findViewById<BoomMenuButton>(R.id.bmb)

        bmb.setButtonEnum(ButtonEnum.Ham)
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_3)
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3)

        bmb.clearBuilders()

        for (i in 0 until bmb.piecePlaceEnum.pieceNumber()) {
            val builder = HamButton.Builder()
                .normalImageRes(R.drawable.uw_logo)
                .normalText("Item ${i + 1}")
                .normalColor(Color.RED)
                .listener { index ->
                    Toast.makeText(this, "Clicked item $index", Toast.LENGTH_SHORT).show()
                }

            bmb.addBuilder(builder)
        }

    }


}