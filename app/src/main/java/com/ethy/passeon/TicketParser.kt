package com.ethy.passeon

import java.util.regex.Pattern

// ✨ 偵探社總部：一個專門用來解析各種票券的物件
object TicketParser {

    // ✨ 這是對外的主要窗口，我們把 parseTicket 改名為更簡潔的 parse
    fun parse(text: String): Map<String, Any> {
        // 優先判斷是否為台鐵
        if (isDefinitelyTRA(text)) {
            return parseTraTicket(text)
        }
        // 如果不是台鐵，再判斷是否為高鐵
        else if (isProbablyTHSR(text)) {
            return parseThsrTicket(text)
        }
        // 都不是，回報無法解析
        return emptyMap()
    }

    // --- 以下是各位專家的實作細節，它們都是 private，代表只有總部內部的人能呼叫 ---

    private fun isDefinitelyTRA(text: String): Boolean {
        val traTrainTypes = listOf("普悠瑪", "太魯閣", "自強", "莒光", "區間")
        val traCodeTypes = listOf("訂票代碼", "台鐵", "臺鐵")
        if (traTrainTypes.any { text.contains(it) }) return true
        if (traCodeTypes.any { text.contains(it) }) return true
        return false
    }

    private fun isProbablyTHSR(text: String): Boolean {
        val hasThsrStation = StationData.thsrStations.any { text.contains(it) }
        val ticketNumberPattern = Pattern.compile("\\d{2}-\\d{1}-\\d{2}-\\d{1}-\\d{3}-\\d{4}")
        val hasTicketNumber = ticketNumberPattern.matcher(text).find()
        if (hasThsrStation && hasTicketNumber) return true
        if (text.contains("訂位代號")) return true
        if (StationData.thsrStations.any { text.contains(it) }) return true
        return false
    }

    private fun parseTraTicket(text: String): Map<String, Any> {
        val result = mutableMapOf<String, String>()
        result["type"] = "台鐵"

        // 解析起訖站
        val traStations = StationData.traStations
        val lines = text.split("\n").filter { it.isNotBlank() }
        for (line in lines) {
            if (line.contains("-")) {
                val parts = line.split("-").map { it.trim() }
                if (parts.size == 2) {
                    val originCandidate = parts[0].replace(Regex("\\d{2}:\\d{2}"), "").trim()
                    val destCandidate = parts[1].replace(Regex("\\d{2}:\\d{2}"), "").trim()
                    val finalOrigin = traStations.find { it == originCandidate }
                    val finalDest = traStations.find { it == destCandidate }
                    if (finalOrigin != null && finalDest != null) {
                        result["title"] = "$finalOrigin → $finalDest"
                        result["origin"] = finalOrigin
                        result["destination"] = finalDest
                        // ✨ 找到後就不用繼續了，但我們先不跳出，讓後面的邏輯有機會覆蓋更精準的結果
                    }
                }
            }
        }

        // ✨ [修正] 如果上面的主要方法沒找到，就用備用方法，並確保寫入 origin 和 destination
        if (!result.containsKey("title")) {
            val foundStations = traStations.filter { text.contains(it) }.distinct()
            if (foundStations.size >= 2) {
                val origin = foundStations[0]
                val destination = foundStations[1]
                result["title"] = "$origin → $destination"
                result["origin"] = origin
                result["destination"] = destination
            }
        }


        // 解析日期和時間
        Regex("(\\d{4})[./-](\\d{2})[./-](\\d{2})").find(text)?.let { result["date"] = it.value }
        text.split("\n").drop(2).joinToString("\n").let { body ->
            Regex("(\\d{2}:\\d{2})").find(body)?.let { result["time"] = it.value }
        }

        // 解析詳細資訊
        var trainNoMatch = Regex("車次\\s*[:：]?\\s*(\\d+)").find(text)
        if (trainNoMatch == null) { trainNoMatch = Regex("(\\d+)次").find(text) }
        trainNoMatch?.let { result["車次"] = it.groupValues[1] }

        val carSeatRegex = Regex("^(\\d{1,2})\\s+(\\d{1,3})")
        for (line in lines) {
            val match = carSeatRegex.find(line)
            if (match != null) {
                result["車廂"] = match.groupValues[1]
                result["座位"] = match.groupValues[2]
                break
            }
        }
        if (!result.containsKey("車廂")) {
            Regex("(\\d+)\\s*車\\s*(\\d+)\\s*號").find(text)?.let {
                result["車廂"] = it.groupValues[1]
                result["座位"] = it.groupValues[2]
            }
        }
        return result
    }

    private fun parseThsrTicket(text: String): Map<String, Any> {
        val result: MutableMap<String, Any> = parseBasicInfo(text, StationData.thsrStations, "高鐵").toMutableMap()
        val customFields = mutableMapOf<String, String>()
        // --- 解析詳細資訊 (高鐵) ---
        Regex("車次\\s*(\\d+)").find(text)?.let {
            customFields["車次"] = it.groupValues[1] // ✨ 存入魔術袋
        }
        Regex("車廂\\s*(\\d+)").find(text)?.let {
            customFields["車廂"] = it.groupValues[1] // ✨ 存入魔術袋
        }
        Regex("座位\\s*([A-Z0-9]+)").find(text)?.let {
            customFields["座位"] = it.groupValues[1] // ✨ 存入魔術袋
        }
        result["customFields"] = customFields // ✨ 把整個魔術袋放進最終結果
        return result
    }

    // ✨ parseBasicInfo 的回傳值也改成 Map<String, String>
    private fun parseBasicInfo(text: String, stations: List<String>, type: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        result["type"] = type
        // --- 解析起訖站 ---
        val condensedText = text.replace("\\s".toRegex(), "")
        val foundStationsInOrder = mutableListOf<String>()
        var searchIndex = 0
        while(searchIndex < condensedText.length) {
            val remainingText = condensedText.substring(searchIndex)
            val matchedStation = stations.find { remainingText.startsWith(it) }
            if (matchedStation != null) {
                if (!foundStationsInOrder.contains(matchedStation)) {
                    foundStationsInOrder.add(matchedStation)
                }
                searchIndex += matchedStation.length
            } else {
                searchIndex++
            }
        }
        if (foundStationsInOrder.size >= 2) {
            val origin = foundStationsInOrder[0]
            val destination = foundStationsInOrder[1]
            result["title"] = "$origin → $destination"
            result["origin"] = origin
            result["destination"] = destination
        }

        // --- 解析日期和時間 ---
        val dateRegex = Regex("(\\d{4})[./-](\\d{2})[./-](\\d{2})")
        val timeRegex = Regex("(\\d{2}:\\d{2})")
        dateRegex.find(text)?.let { result["date"] = it.value }
        val textBody = text.split("\n").drop(2).joinToString("\n")
        timeRegex.find(textBody)?.let { result["time"] = it.value }

        return result
    }
}

