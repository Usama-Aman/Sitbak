package com.android.sitbak.network


import android.util.Log
import com.android.sitbak.base.AppController
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject

class SocketIO {


    companion object {
        const val socketBaseUrl = "http://178.128.29.7:5333/?"
        const val socketSendMessage = "message_send"
        const val socketGetMessage = "message_get"
        const val socketOrderSendStatus = "send_delivery_status"
        const val socketOrderGetStatus = "get_delivery_status"
        const val socketSendLocation = "send_location"
        const val socketGetDriverLocation = "driver_location"
    }

    private val opts = IO.Options()
    private var mSocket: Socket

    init {
        opts.query = "id=${AppController.userDetails?.id}&user_type=user"
        mSocket = IO.socket(socketBaseUrl, opts)
    }

    fun on(name: String, callback: (JSONObject) -> Unit) {

        if (!mSocket.connected()) {
            mSocket.connect()
            Log.e("Socket", "Connect")
        }
        Log.e("Socket", "Connected")
        mSocket.on(name) { args -> callback(JSONObject(args.first().toString())) }
    }

    fun emit(name: String, data: JSONObject) {
        if (!mSocket.connected()) {
            mSocket.connect()
        }
        mSocket.emit(name, data)
    }

    fun isConnected() = mSocket.connected()

    fun offSocketEvent(event: String) {
        mSocket.off(event)
    }

    fun disconnect() {
        mSocket.off()
        mSocket.close()
        mSocket.disconnect()
        Log.e("Socket", "Disconnected")
    }
}