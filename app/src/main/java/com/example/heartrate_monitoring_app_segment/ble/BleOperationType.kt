/**
Copyright [2022] [Eugene John, Joseph Matthew Espinas, Ramon Carmelo Y. Calimbahin, Randy Lance O. Zebroff ]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.example.heartrate_monitoring_app_segment.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import java.util.UUID

sealed class BleOperationType {
    abstract val device: BluetoothDevice
}

data class Connect(override val device: BluetoothDevice, val context: Context) : BleOperationType()

data class Disconnect(override val device: BluetoothDevice) : BleOperationType()
data class EnableNotifications(
    override val device: BluetoothDevice,
    val characteristicUuid: UUID
) : BleOperationType()

data class DisableNotifications(
    override val device: BluetoothDevice,
    val characteristicUuid: UUID
) : BleOperationType()