package com.example.data

import org.json.JSONObject

data class QRResult(val sku: String, val code: String)

object QRParser {
    fun parse(text: String): QRResult? {
        return try {
            val json = JSONObject(text)
            val sku = json.optString("sku").trim()
            val code = json.optString("code").trim()
            
            if (sku.isEmpty() || code.isEmpty()) {
                null
            } else {
                QRResult(sku, code)
            }
        } catch (e: Exception) {
            null
        }
    }
}
