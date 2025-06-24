package com.ethy.passeon

import java.util.regex.Pattern

// ✨ 偵探社總部：一個專門用來解析各種票券的物件
object TicketParser {

    // ✨ 這是對外的主要窗口，我們把 parseTicket 改名為更簡潔的 parse
    fun parse(text: String): Map<String, String> {
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

    private fun parseTraTicket(text: String): Map<String, String> {
        val result = parseBasicInfo(text, StationData.traStations, "台鐵")
        val lines = text.split("\n")
        // --- 解析詳細資訊 (台鐵) ---
        // ✨ [修改] 新增「XXX次」的判斷邏輯
        var trainNoMatch = Regex("車次\\s*(\\d+)").find(text)
        if (trainNoMatch == null) {
            // 如果找不到「車次 XXX」，就試著找「XXX次」
            trainNoMatch = Regex("(\\d+)次").find(text)
        }
        trainNoMatch?.let {
            result["trainNo"] = it.groupValues[1]
        }
        // ✨ [修改] 採用新的「純數字規律」來辨識車廂和座位
        val carSeatRegex = Regex("^(\\d{1,2})\\s+(\\d{1,3})") // 尋找 "數字<空格>數字" 格式的行
        for (line in lines) {
            val match = carSeatRegex.find(line)
            if (match != null) {
                result["carNo"] = match.groupValues[1]
                result["seatNo"] = match.groupValues[2]
                break // 現在 break 是直接在 for 迴圈裡，合法！
            }
        }
        // 如果上面的方法找不到，再用「X車X號」當作備用方案
        if (!result.containsKey("carNo")) {
            Regex("(\\d+)\\s*車\\s*(\\d+)\\s*號").find(text)?.let {
                result["carNo"] = it.groupValues[1]
                result["seatNo"] = it.groupValues[2]
            }
        }
        return result
    }

    private fun parseThsrTicket(text: String): Map<String, String> {
        val result = parseBasicInfo(text, StationData.thsrStations, "高鐵")
        // --- 解析詳細資訊 (高鐵) ---
        Regex("車次\\s*(\\d+)").find(text)?.let {
            result["trainNo"] = it.groupValues[1]
        }
        Regex("車廂\\s*(\\d+)").find(text)?.let {
            result["carNo"] = it.groupValues[1]
        }
        Regex("座位\\s*([A-Z0-9]+)").find(text)?.let {
            result["seatNo"] = it.groupValues[1]
        }
        return result
    }

    // ✨ [新增] 為了避免重複，我們把解析基本資訊的邏輯抽出來變成一個共用函式
    private fun parseBasicInfo(text: String, stations: List<String>, type: String): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()

        // 解析起訖站
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
            result["title"] = "${foundStationsInOrder[0]} → ${foundStationsInOrder[1]}"
            result["type"] = type
        }

        // 解析日期和時間
        val dateRegex = Regex("(\\d{4})[./-](\\d{2})[./-](\\d{2})")
        val timeRegex = Regex("(\\d{2}:\\d{2})")
        dateRegex.find(text)?.let { result["date"] = it.value }
        val textBody = text.split("\n").drop(2).joinToString("\n")
        timeRegex.find(textBody)?.let { result["time"] = it.value }

        return result
    }
}

