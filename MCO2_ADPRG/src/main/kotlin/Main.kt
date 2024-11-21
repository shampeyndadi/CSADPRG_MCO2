import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.WordFrequency
import com.kennycason.kumo.bg.CircleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.LinearFontScalar
import com.kennycason.kumo.palette.ColorPalette
import org.jfree.chart.ChartUtils
import java.awt.Color
import java.awt.Dimension
import org.jfree.chart.ChartFactory
import org.jfree.chart.ui.RectangleEdge
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.Font
import java.io.File

fun loadCSV(filePath: String): ArrayList<Map<String, String>> {
    val lines = File(filePath).readLines()
    val headers = lines.first().split(",")
    val records = ArrayList<Map<String, String>>()

    for (line in lines.drop(1)) {
        val values = line.split(",")
        val record = HashMap<String, String>()
        for (i in headers.indices) {
            record[headers[i]] = values[i]
        }
        records.add(record)
    }
    return records
}

fun createTopWordCloud(wordFreq: Map<String, Int>, outputPath: String, topN: Int = 20) {
    val topWords = wordFreq.entries
        .sortedByDescending { it.value }
        .take(topN)
        .map { WordFrequency(it.key, it.value) }

    val dimension = Dimension(800, 800)
    val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)
    wordCloud.setBackgroundColor(Color.BLACK)
    wordCloud.setPadding(2)
    wordCloud.setBackground(CircleBackground(400))

    val font = Font("Arial", Font.BOLD, 20)
    val secondaryFont = Font("Courier New", Font.ITALIC, 18)
    wordCloud.setKumoFont(KumoFont(font))
    wordCloud.setKumoFont(KumoFont(secondaryFont))

    val colors = listOf(
        Color(0x336699),
        Color(0x99CC00),
        Color(0xFF6600),
        Color(0xFFCC00),
        Color(0x9999CC)
    )
    wordCloud.setColorPalette(ColorPalette(colors))

    wordCloud.setFontScalar(LinearFontScalar(10, 80))
    wordCloud.build(topWords)
    wordCloud.writeToFile(outputPath)

}


fun createPostsBarChart(postsPerMonth: Map<String, Int>, outputPath: String) {
    val dataset = DefaultCategoryDataset()
    postsPerMonth.forEach { (month, count) ->
        dataset.addValue(count.toDouble(), "Posts", month)
    }

    val chart = ChartFactory.createBarChart(
        "Total Posts Per Month",
        "Month",
        "Count",
        dataset
    )
    chart.title.font = chart.title.font.deriveFont(16f)
    ChartUtils.saveChartAsPNG(File(outputPath), chart, 1500, 600)
    println("Bar chart saved to: $outputPath")
}

fun createSymbolPieChartWithLegend(charFreq: Map<String, Int>, outputPath: String) {
    val symbols = charFreq.filterKeys { it.matches(Regex("[^\\p{L}\\p{N}\\s]")) }

    val filteredSymbols = mutableMapOf<String, Int>()
    var otherSymbolsCount = 0

    symbols.forEach { (symbol, freq) ->
        if (symbol.isBlank() || freq < 5) {
            otherSymbolsCount += freq
        } else {
            filteredSymbols[symbol] = freq
        }
    }

    if (otherSymbolsCount > 0) {
        filteredSymbols["Other Symbols"] = otherSymbolsCount
    }

    val dataset = DefaultPieDataset<String>()
    filteredSymbols.forEach { (symbol, freq) ->
        dataset.setValue(symbol, freq.toDouble())
    }

    val chart = ChartFactory.createPieChart(
        "Symbol Frequency Distribution",
        dataset,
        true,
        true,
        false
    )

    val legend = chart.legend
    legend.position = RectangleEdge.RIGHT
    legend.itemFont = chart.title.font.deriveFont(12f)

    ChartUtils.saveChartAsPNG(File(outputPath), chart, 800, 600)
}



fun countWords(lines: ArrayList<String>): Int {
    var wordCount = 0
    for (line in lines){
        val wordArray = line.split("\\s+".toRegex())
        wordCount += wordArray.size
    }

    return wordCount
}

fun countVocabSize(lines: ArrayList<String>): Int{
    val uniqueWords = hashSetOf<String>()

    for (line in lines){
        val wordArray = line.split("\\s+".toRegex())

        for (word in wordArray){
            if (word.isNotEmpty()) {
                uniqueWords.add(word)
            }
        }
    }

    return uniqueWords.size
}

fun wordFrequency(lines: ArrayList<String>): Map<String, Int>{
    val frequencies = HashMap<String, Int>()
    for (line in lines){
        val wordArray = line.split("\\s+".toRegex())
        for (word in wordArray){
            if (word.isNotEmpty()) {
                val lowerCaseWord = word.lowercase()
                frequencies[lowerCaseWord] = frequencies.getOrDefault(lowerCaseWord, 0) + 1
            }
        }
    }

    return frequencies.entries
        .sortedByDescending { it.value }
        .associate { it.toPair() }
}

fun characterFrequency(lines: ArrayList<String>): Map<String, Int> {
    val frequencies = HashMap<String, Int>()
    for (line in lines) {
        val lowerCase = line.lowercase()
        for (char in lowerCase) {
            if (char != ' ') {
                val key = char.toString() // Convert Char to String
                frequencies[key] = frequencies.getOrDefault(key, 0) + 1
            }
        }
    }

    return frequencies.entries
        .sortedByDescending { it.value }
        .associate { it.toPair() }
}

fun symbolFrequency(lines: ArrayList<String>): Map<String, Int> {
    val symbolFrequencies = HashMap<String, Int>()
    val symbolRegex = Regex("[^\\w\\s]") // Matches non-word, non-whitespace characters

    for (line in lines) {
        for (char in line) {
            val charStr = char.toString()
            if (charStr.matches(symbolRegex)) {
                symbolFrequencies[charStr] = symbolFrequencies.getOrDefault(charStr, 0) + 1
            }
        }
    }

    return symbolFrequencies.entries
        .sortedByDescending { it.value }
        .associate { it.toPair() }
}


fun postPerMonth(dates: ArrayList<String>): Map<String, Int> {
    val monthlyPosts = HashMap<String, Int>()
    for (date in dates) {
        if (date.isNotBlank() && date.length >= 7) {
            val month = date.substring(0, 7)
            if (month.matches(Regex("\\d{4}-\\d{2}"))) {
                monthlyPosts[month] = monthlyPosts.getOrDefault(month, 0) + 1
            }
        }
    }
    return monthlyPosts.toSortedMap()
}


fun topWords(frequencies: Map<String, Int>, topN: Int = 20): List<Pair<String, Int>> {
    val frequencyList = ArrayList(frequencies.entries)

    for (i in 0 until frequencyList.size) {
        for (j in 0 until frequencyList.size - i - 1) {
            if (frequencyList[j].value < frequencyList[j + 1].value) {
                val temp = frequencyList[j]
                frequencyList[j] = frequencyList[j + 1]
                frequencyList[j + 1] = temp
            }
        }
    }

    val topWords = ArrayList<Pair<String, Int>>()
    for (i in 0 until topN) {
        if (i >= frequencyList.size)
            break
        topWords.add(Pair(frequencyList[i].key, frequencyList[i].value))
    }

    return topWords
}

fun stopWords(frequencies: Map<String, Int>, stopWords: List<String>): Map<String, Boolean> {
    val result = HashMap<String, Boolean>()
    for (stopWord in stopWords) {
        result[stopWord] = frequencies.containsKey(stopWord.lowercase())
    }
    return result
}

fun main(){

    val filePath = "C:\\Users\\admin\\Downloads\\MCO2_ADPRG\\src\\main\\kotlin\\fake_tweets.csv"

    val stopWordsList = listOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
        "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "can't", "cannot",
        "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few",
        "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
        "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll",
        "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most",
        "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
        "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should",
        "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves",
        "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through",
        "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
        "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
        "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your",
        "yours", "yourself", "yourselves"
    )

    val data = loadCSV(filePath)
    val tweets = ArrayList<String>()
    val dates = ArrayList<String>()

    for (record in data){
        if (record.containsKey("text")){
            tweets.add(record["text"]!!)
        }

        if (record.containsKey("date_created") && record["date_created"]!!.isNotBlank()) {
            dates.add(record["date_created"]!!)
        }
    }

    val wordFreq = wordFrequency(tweets)
    createTopWordCloud(wordFreq, "top20_wordcloud.png")

    val postsPerMonth = postPerMonth(dates)
    createPostsBarChart(postsPerMonth, "posts_barchart.png")

    val symbolFreq = symbolFrequency(tweets)
    createSymbolPieChartWithLegend(symbolFreq, "symbol_piechart.png")

    println("Word Count: ${countWords(tweets)}\n")

    println("Vocabulary Size: ${countVocabSize(tweets)}\n")

    println(symbolFreq)

    println("Word Frequency:")
    for ((word, freq) in wordFreq) {
        println("$word: $freq")
    }
    println()

    println("Top 20 Words:")
    val topWordsList = topWords(wordFreq, 20)

    for ((word, freq) in topWordsList) {
        println("$word: $freq")
    }
    println()

    val charFreq = characterFrequency(tweets)

    println("Character Frequencies:")
    for ((char, freq) in charFreq) {
        println("$char: $freq")
    }
    println()

    val stopWordsPresent = stopWords(wordFreq, stopWordsList)
    println("Stop Words:")
    for ((word, isPresent) in stopWordsPresent){
        println("$word: ${if (isPresent) "Yes" else "No"}")
    }
    println()

}
