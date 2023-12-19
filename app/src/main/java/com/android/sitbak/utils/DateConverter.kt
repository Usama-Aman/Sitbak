@file:Suppress("DEPRECATION", "unused", "FunctionName", "LocalVariableName")

package com.android.sitbak.utils

import android.util.Log
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object DateConverter {
    val currentDate: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
            timeFormat.timeZone = TimeZone.getTimeZone("UTC")
            return timeFormat.format(date)
        }

    val currentDateNew: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            timeFormat.timeZone = TimeZone.getTimeZone("UTC")
            return timeFormat.format(date)
        }

    val tomorrowDateInMillis: Long
        get() {
            var date = Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DATE, 1)

            date = calendar.time
            return calendar.timeInMillis
        }

    val currentMinutes: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            val timeFormat = SimpleDateFormat("mm", Locale.ENGLISH)
            //     timeFormat.timeZone = TimeZone.getTimeZone("UTC")
            return timeFormat.format(date)
        }

    val currentHours: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            val timeFormat = SimpleDateFormat("HH", Locale.ENGLISH)
            // timeFormat.timeZone = TimeZone.getTimeZone("UTC")
            return timeFormat.format(date)
        }


    val currentSeconds: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            val timeFormat = SimpleDateFormat("ss", Locale.ENGLISH)
            //timeFormat.timeZone = TimeZone.getTimeZone("UTC")
            return timeFormat.format(date)
        }


    //    val currentDateWithoutTime: Date
//        get() {
//            val calendar = Calendar.getInstance()
//            val date = calendar.time
//            val timeFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
//            //            timeFormat.timeZone = TimeZone.getTimeZone("UTC")
//            return getDate(timeFormat.format(date))
//        }
    val currentDateMessage: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            timeFormat.timeZone = TimeZone.getTimeZone("UTC")
            return timeFormat.format(date)
        }

    val currentDateFormated: String
        get() {
            val calendar = Calendar.getInstance()
            val date = calendar.time

            val timeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            return timeFormat.format(date)

        }

    fun getMonthsAgo(monthsAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)

        return calendar.time
    }

    fun DateFormateThree(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun DateFormatFive(date_string: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }
//        val timeFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return myDate ?: Date()
    }


    fun DateFormatFour(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }


    fun ConvertDateFormat(date_string: String): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun ConvertDateFormat2(date_string: String): String {
        //"2020-05-29
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("EE, dd MMM yyyy", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }
fun convertMonthNameInToMonthNumber(monthName:String): Int {
    val date = SimpleDateFormat("MMMM").parse(monthName)
    val cal = Calendar.getInstance()
    cal.time = date
    return (cal[Calendar.MONTH])
}
    fun ConvertDateFormat3(date_string: String): String {
        //"2020-05-29
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }


    fun convertToFromTimePicker(date_string: String): String {
        val dateFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun convertTimeForTimePicker(date_string: String): Date {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        return myDate
    }

    fun getDateDifferenceInMinutes(
        firstDate: String,
        secondDate: String
//        callback: (Long) -> Unit
    ): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate1: Date? = null
        var myDate2: Date? = null
        try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            myDate1 = dateFormat.parse(firstDate)
            myDate2 = dateFormat.parse(secondDate)

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val diff: Long = (myDate1 ?: Date()).time - (myDate2 ?: Date()).time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        //callback(minutes)
        return seconds
    }


    fun getTimeDifferenceWithCurrentTimeInSeconds(
        firstDate: String
    ): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate1: Date? = null
        var myDate2: Date? = null
        try {

            myDate1 = dateFormat.parse(firstDate)
            myDate2 = Date()

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val diff: Long = (myDate1 ?: Date()).time - (myDate2 ?: Date()).time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        //callback(minutes)
        return seconds
    }

    fun convertDateToMillis(date: String): Long {
        // Create a DateFormatter object for displaying date in specified format.

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate: Date? = null
        try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            myDate = dateFormat.parse(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return myDate?.time ?: 0
    }

    fun getDate(date: String, dateFormat: String?): Date {
        // Create a DateFormatter object for displaying date in specified format.


        val formatter = SimpleDateFormat(dateFormat)
        formatter.timeZone = TimeZone.getTimeZone("UTC");

        val myDate: Date
        myDate = try {
            formatter.parse(date) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        return myDate
    }

    fun getMillisToDate(milliSeconds: Long, dateFormat: String?): String {
        // Create a DateFormatter object for displaying date in specified format.

        val formatter = SimpleDateFormat(dateFormat)
        formatter.timeZone = TimeZone.getTimeZone("UTC");
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun DateFormateInMilis(date_string: String): Long {

        val calendar = Calendar.getInstance()
        var timeInMillis: Long = 0


        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate: Date? = null
        try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            myDate = dateFormat.parse(date_string)
            calendar.time = myDate
            timeInMillis = calendar.timeInMillis
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        // val timeFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return timeInMillis
    }

    fun getYear(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getMonth(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getMonthNo(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("MM", Locale.getDefault())
        return timeFormat.format(myDate)
    }


    fun getLastSixMonths(date_string: String): ArrayList<Int> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        var cal = Calendar.getInstance()
        var monthsList = ArrayList<Int>()
        for (i in 0..5) {
            monthsList.add(cal.get(Calendar.MONTH) + 1)
            cal.add(Calendar.MONTH, -1)
        }

        Log.e("PreviousSixMonths", monthsList.joinToString())

//        val timeFormat = SimpleDateFormat("MM", Locale.getDefault())
//        return timeFormat.format(myDate)
        return monthsList
    }

    fun getDay(date_string: String): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("dd", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getDayNew(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("dd", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getHours(date_string: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("HH", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getMinutes(date_string: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");

            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("mm", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getSeconds(date_string: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");

            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("ss", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getAmPm(date_string: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");

            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("a", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getTimeWithAmPm(date_string: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");

            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getDayName(date_string: String): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val myDate: Date
        myDate = try {
            dateFormat.parse(date_string) ?: Date()
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }

        val timeFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun getConvertedTime(date_string: String): Date {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return myDate ?: Date()
    }

    fun convertTime24(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun convertTime12(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun convertDate(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("MM.dd.yyyy hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }


    fun getDelighterTime(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun getConvertedDate(date_string: Date): String {
        var dateString = date_string
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(dateString)
    }

    fun getConvertedDate(date_string: String): Date {
        var dateString = date_string
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return myDate ?: Date()
    }


    fun converttimeWithOutT(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val myDate = try {
            dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
            Calendar.getInstance().time
        } catch (e1: NullPointerException) {
            e1.printStackTrace()
            Calendar.getInstance().time
        }

        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate)
    }

    fun convertTime_hour(date_string: String): String {
        //        date_string = onUtcToLocal(date_string)
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }


    fun compareTime(time1: String, time2: String): Boolean {
        var c1 = Calendar.getInstance()
        var c2 = Calendar.getInstance()
        c1.time = getTime(time1)
        c2.time = getTime(time2)
        return c1 < c2
    }

    fun compareDateWithCurrentTime(time1: String): Boolean {
        var c1 = Calendar.getInstance()
        var currentTime = Calendar.getInstance()
        c1.time = getDateObjectInFullFormat(time1)
        c1.time.hours = (c1.time.hours - 12)
        return c1 >= currentTime
    }

    private fun getTime(date_string: String): Date {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("hh:mm aaa", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

//        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        return myDate ?: Date()
    }

    fun convertDateWithCompleteDetails(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("EEEE,dd,MMMM,yyyy, hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun convertDateWithTime(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("dd MMMM,yyyy hh:mm aa", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun convertDateMonth(date_string: String): String {
        var dateString = date_string

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(dateString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun getDateFullFormate(date_string: String): String? {
        //        date_string = onUtcToLocal(date_string)!!
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("EE, dd MMM yyyy", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun getDateFormat(date_string: String): Date? {
        //        date_string = onUtcToLocal(date_string)!!
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }


        return myDate
    }

    fun getDateFullFormateOne(date_string: String): String? {

        //        date_string = onUtcToLocal(date_string)!!
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("MMMM d'th'", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    private fun getDateObjectInFullFormat(date_string: String): Date {
        var mDate = onUtcToLocal(date_string)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(mDate)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return myDate ?: Date()
    }


    fun getDateFromString(date_string: String): String? {
        //        date_string = onUtcToLocal(date_string)!!
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var myDate: Date? = null
        try {
            dateFormat.timeZone = TimeZone.getTimeZone("UTC");
            myDate = dateFormat.parse(date_string)


        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun getDateFullFormateThree(date_string: String): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        return timeFormat.format(myDate ?: Date())
    }

    fun getCustomDateString(date: String, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val dateFormated: Date? = dateFormat.parse(date)
        val tmp = SimpleDateFormat("MMMM d", Locale.getDefault())
        var str = tmp.format(dateFormated ?: Date())
        str = str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1)
        str += if (dateFormated!!.date > 10 && dateFormated.date < 14) "th"
        else {
            when {
                str.endsWith("1") -> "st"
                str.endsWith("2") -> "nd"
                str.endsWith("3") -> "rd"
                else -> "th"
            }
        }
        return str
    }

    fun getCustomDateString(): String {
        val date: Date? = Date()
        val tmp = SimpleDateFormat("MMMM d", Locale.getDefault())
        var str = tmp.format(date ?: Date())
        str = str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1)
        str += if (date!!.date > 10 && date.date < 14) "th"
        else {
            when {
                str.endsWith("1") -> "st"
                str.endsWith("2") -> "nd"
                str.endsWith("3") -> "rd"
                else -> "th"
            }
        }
        return str
    }

    fun onLocalToUtc(s: String): String? {
        try {
            val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            utcFormat.timeZone = TimeZone.getDefault()
            val date: Date?
            date = utcFormat.parse(s)
            val localTimeFormate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
            localTimeFormate.timeZone = TimeZone.getTimeZone("UTC")
            localTimeFormate.parse(localTimeFormate.format(date ?: Date()))
            //            prettyTime.
            return localTimeFormate.format(date ?: Date())
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    fun tierNew(s: String): String? {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(s)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return output.format(d ?: Date())
    }

    private fun onUtcToLocal(s: String): String {
        try {
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date?
            date = utcFormat.parse(s)
            val localTimeFormate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            localTimeFormate.timeZone = TimeZone.getDefault()
            localTimeFormate.parse(localTimeFormate.format(date ?: Date()))
            //            prettyTime.
            return localTimeFormate.format(date ?: Date())
        } catch (e: ParseException) {
            try {
                val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                utcFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date: Date?
                date = utcFormat.parse(s)
                val localTimeFormate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                localTimeFormate.timeZone = TimeZone.getDefault()
                localTimeFormate.parse(localTimeFormate.format(date ?: Date()))
                //            prettyTime.
                return localTimeFormate.format(date ?: Date())
            } catch (ex: ParseException) {
                ex.printStackTrace()
            }
            //
        }

        return ""
    }

    fun validateFormat(trim: String): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        var date: Date? = null
        try {
            date = dateFormat.parse(trim)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val tmp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return tmp.format(date ?: Date())

    }

    fun setEventEditDate(trim: String): String {
        val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date: Date? = null
        try {
            date = parseFormat.parse(trim)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val timeFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return timeFormat.format(date ?: Date())
    }

    fun convertSpecialWallTop(date_string: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val myDate: Date?
        try {
            myDate = dateFormat.parse(date_string)

        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

        val timeFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        val calendaras = Calendar.getInstance()
        calendaras.time = myDate ?: Date()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)


        return if (calendaras.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendaras.get(
                Calendar.DAY_OF_YEAR
            ) == today.get(Calendar.DAY_OF_YEAR)
        ) {
            "Today"
        } else if (calendaras.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendaras.get(
                Calendar.DAY_OF_YEAR
            ) == yesterday.get(Calendar.DAY_OF_YEAR)
        ) {
            "Yesterday"
        } else {
            timeFormat.format(myDate ?: Date())
        }
    }


    private fun convertToDateObj(date_string: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            return dateFormat.parse(date_string)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return Date()
    }

    fun convertPaymentsTime(
        date_string: String,
        newFormat: String = "dd MMM ',' yyyy 'at' hh:mm a"
    ): String? { //yyyy-MM-dd HH:mm a
        val dataTime = onUtcToLocal(date_string)
        val dateFormat = SimpleDateFormat(newFormat, Locale.getDefault())
        val dateFormatOld = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val dateObj = dateFormatOld.parse(dataTime)
            return dateFormat.format(dateObj ?: Date())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertSubscrbedTime(date_string: String): String? {
        val dataTime = onUtcToLocal(date_string)
        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val dateFormatOld = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            var dateObj = dateFormatOld.parse(dataTime)
            var calendar: Calendar = Calendar.getInstance()
            calendar.time = dateObj
            calendar.add(Calendar.MONTH, 1)

            return dateFormat.format(calendar.time ?: Date())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }


    fun convertSupportAdapter(date_string: String): String? {
        val dataTime = onUtcToLocal(date_string)
        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val dateFormatOld = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            var dateObj = dateFormatOld.parse(dataTime)
            var calendar: Calendar = Calendar.getInstance()
            calendar.time = dateObj

            return dateFormat.format(calendar.time ?: Date())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertNotificationTime(date_string: String): String? {
        val dataTime = onUtcToLocal(date_string)
        val dateFormat = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault())
        val dateFormatOld = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val dateObj = dateFormatOld.parse(dataTime)
            return dateFormat.format(dateObj ?: Date())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun convertToDateObjForTime(date_string: String): String? {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormatOld = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val dateObj = dateFormatOld.parse(date_string)
            return dateFormat.format(dateObj ?: Date())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getTicketCreatedTime(date: String): String {
        val localDate = onUtcToLocal(date)
        val dtObj = convertToDateObjForTime(localDate)
        return dtObj ?: ""
    }

    fun timeAgo(date: String): String {
        val p = PrettyTime(Locale.getDefault())
        val localDate = onUtcToLocal(date)
        val dtObj = convertToDateObj(localDate)
        return p.format(dtObj)
    }

    fun getAge(dobString: String): Int {
        var date: Date? = null
        var sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        date = try {
            sdf.parse(dobString)
        } catch (e: ParseException) {
            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            try {
                sdf.parse(dobString)
            } catch (ex: ParseException) {
                Date()
            }
        }

        if (date == null) return 0
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob.time = date
        val year = dob.get(Calendar.YEAR)
        val month = dob.get(Calendar.MONTH)
        val day = dob.get(Calendar.DAY_OF_MONTH)
        dob.set(year, month + 1, day)
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    fun checkIfCardDateIsValid(trim: String): Boolean {
        val isValid: Boolean
        val dateFormatOld = SimpleDateFormat("MM/yy", Locale.getDefault())
        isValid = try {
            val dateObj = dateFormatOld.parse(trim)
            val dateNow = Calendar.getInstance().time
            dateObj?.after(dateNow) != false
        } catch (e: ParseException) {
            e.printStackTrace()
            true
        }
        return isValid
    }
}
