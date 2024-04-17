package com.example.bluetoothapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.example.bluetoothapp.databinding.FragmentDeviceManagerBinding
import java.util.Timer
import java.util.TimerTask
import java.util.UUID


class DeviceManagerFragment : Fragment() {

    private var binding: FragmentDeviceManagerBinding? = null

    private val writeServiceUUID: UUID = UUID.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c")
    //private val writeServiceUUID: UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
    private val writeCharacteristicUUID: UUID = UUID.fromString("569a2001-b87f-490c-92cb-11ba5ea5167c")
    //private val writeCharacteristicUUID: UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
    private val indicateServiceUUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val readCharacteristicUUID: UUID = UUID.fromString("569a2000-b87f-490c-92cb-11ba5ea5167c")
    private val indicateCharacteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private val bleDevice: BleDevice? by lazy {
        arguments?.getParcelable(HomeFragment.BLE_DEVICE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDeviceManagerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btAlert?.setOnClickListener {
            bleWrite()
        }

        bleDevice?.let {
            iTagRssi()
            bleIndicate()
        }
    }

    private fun bleRead()
    {
        BleManager.getInstance().read(
            bleDevice,
            writeServiceUUID.toString(),
            readCharacteristicUUID.toString(),
            object : BleReadCallback() {
                override fun onReadSuccess(data: ByteArray) {

                }
                override fun onReadFailure(exception: BleException) {

                }
            })
    }

    private fun bleRssi() {
        BleManager.getInstance().readRssi(
            bleDevice,
            object : BleRssiCallback() {
                override fun onRssiFailure(exception: BleException) {}
                override fun onRssiSuccess(rssi: Int) {
                    println(rssi)
                    when(rssi){

                        in -69..-1 -> binding?.txtDistance?.text = "-1m"
                        in -80..-70 -> binding?.txtDistance?.text = "1m"
                        in -89..-81 -> binding?.txtDistance?.text = "ARif"
                        else -> binding?.txtDistance?.text = "+3m"
                    }
                }
            })
    }


    private fun bleIndicate() {


        BleManager.getInstance().indicate(
            bleDevice,
            indicateServiceUUID.toString(),
            indicateCharacteristicUUID.toString(),
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    Toast.makeText(requireContext(), "Device Button pressed", Toast.LENGTH_LONG).show()
                }
                override fun onIndicateFailure(exception: BleException?) {}
                override fun onCharacteristicChanged(data: ByteArray?) {
                    Toast.makeText(requireContext(), "Device Button pressed", Toast.LENGTH_LONG).show()
                }

            }
        )
    }

    private fun bleWrite(){ //send data to device method
        BleManager.getInstance().write(
            bleDevice,
            writeServiceUUID.toString(),
            writeCharacteristicUUID.toString(),
            HexUtil.hexStringToBytes("test"),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    println(current)
                    println(total)
                    println(justWrite)
                }
                override fun onWriteFailure(exception: BleException?) {}
            }
        )

    }

    private fun iTagRssi() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                bleDevice?.let { bleRssi() }
            }
        },0,1000)
    }
}