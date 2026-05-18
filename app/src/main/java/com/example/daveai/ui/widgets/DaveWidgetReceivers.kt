package com.example.daveai.ui.widgets

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DaveMicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveMicWidget()
}

class DaveActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveActionsWidget()
}

class DaveStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveStatsWidget()
}

class DaveFlashlightWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveFlashlightWidget()
}

class DaveHardwareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveHardwareWidget()
}

class DaveSongWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveSongWidget()
}

class DaveHistoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveHistoryWidget()
}

class DaveMasterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DaveMasterWidget()
}
