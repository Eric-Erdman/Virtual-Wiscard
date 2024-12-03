package com.cs407.virtualwiscard;
import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class MyHostApduService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        //Send test code CHANGE
        val responseMessage = "123456"
        return responseMessage.toByteArray(Charsets.UTF_8)
    }

    override fun onDeactivated(reason: Int) {
    }
}
