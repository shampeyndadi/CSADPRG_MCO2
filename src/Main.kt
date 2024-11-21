import java.io.File
import java.nio.file.Paths

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

fun characterFrequency(lines: ArrayList<String>): Map<Char, Int> {
    val frequencies = HashMap<Char, Int>()
    for (line in lines) {
        val lowerCase = line.lowercase()
        for (char in lowerCase) {
            if (char != ' ') {
                frequencies[char] = frequencies.getOrDefault(char, 0) + 1
            }
        }
    }

    return frequencies.entries
        .sortedByDescending { it.value }
        .associate { it.toPair() }
}


fun symbolFrequency(lines: ArrayList<String>): Map<Char, Int>{
    val symbolFrequency = HashMap<Char, Int>()

    for (line in lines){
        for (character in line){
            if (!character.isLetterOrDigit()){
                symbolFrequency[character] = symbolFrequency[character]!! + 1
            }else{
                symbolFrequency[character] = 1
            }
        }
    }

    return symbolFrequency
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


fun postPerMonth(dates: ArrayList<String>): Map<String, Int>{
    val monthlyPosts = HashMap<String, Int>()

    for (date in dates){
        val month : String

        month = date.substring(0, 7)

        if (month.isNotEmpty()){
            monthlyPosts[month] = monthlyPosts[month]!! + 1
        }else{
            monthlyPosts[month] = 1
        }
    }

    return monthlyPosts.toSortedMap()
}

fun stopWords(frequencies: Map<String, Int>, stopWords: List<String>): Map<String, Boolean> {
    val result = HashMap<String, Boolean>()
    for (stopWord in stopWords) {
        result[stopWord] = frequencies.containsKey(stopWord.lowercase())
    }
    return result
}

fun main(){

    val filePath = "/Users/jedidah/Documents/MCO2_ADPRG/src/fake_tweets.csv"
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
        }else{
            dates.add("")
        }

        if (record.containsKey("date")){
            dates.add(record["date"]!!)
        }else{
            dates.add("")
        }
    }

    println("Word Count: ${countWords(tweets)}\n")

    println("Vocabulary Size: ${countVocabSize(tweets)}\n")

    val wordFreq = wordFrequency(tweets)
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
