package com.example.daveai.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.templates.ControlButton
import android.service.controls.templates.ToggleTemplate
import androidx.annotation.RequiresApi
import com.example.daveai.MainActivity
import java.util.concurrent.Flow
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.R)
class DaveControlProviderService : ControlsProviderService() {

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        return Flow.Publisher { subscriber ->
            val control = Control.StatelessBuilder("dave_trigger", createPendingIntent())
                .setTitle("Trigger Dave")
                .setSubtitle("Voice Command")
                .setDeviceType(DeviceTypes.TYPE_LIGHT)
                .build()
            subscriber.onNext(control)
            
            val vaultControl = Control.StatelessBuilder("vault_status", createPendingIntent())
                .setTitle("Vault Status")
                .setSubtitle("Check Memory")
                .setDeviceType(DeviceTypes.TYPE_DISPLAY)
                .build()
            subscriber.onNext(vaultControl)
            
            subscriber.onComplete()
        }
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        return Flow.Publisher { subscriber ->
            controlIds.forEach { id ->
                val control = Control.StatefulBuilder(id, createPendingIntent())
                    .setTitle(if (id == "dave_trigger") "Trigger Dave" else "Vault Status")
                    .setStatus(Control.STATUS_OK)
                    .setControlTemplate(ToggleTemplate(id, ControlButton(false, "Toggle Dave Action")))
                    .build()
                subscriber.onNext(control)
            }
            subscriber.onComplete()
        }
    }

    override fun performControlAction(controlId: String, action: ControlAction, consumer: Consumer<Int>) {
        if (action is BooleanAction) {
            // Trigger Dave or Open Vault
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (controlId == "vault_status") {
                    this.action = "com.example.daveai.VAULT_ACTION"
                }
            }
            startActivity(intent)
            consumer.accept(ControlAction.RESPONSE_OK)
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
