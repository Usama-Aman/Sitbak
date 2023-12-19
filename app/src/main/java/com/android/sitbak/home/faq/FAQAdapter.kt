package com.android.sitbak.home.faq

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.sitbak.R
import com.android.sitbak.utils.viewGone
import com.android.sitbak.utils.viewVisible


class FAQAdapter(
    private val faqList: MutableList<FAQData>,
    private val context: Context,
) : BaseExpandableListAdapter() {


    override fun getGroupCount(): Int = faqList.size

    override fun getChildrenCount(groupPosition: Int): Int = 1

    override fun getGroup(groupPosition: Int): Any = faqList[groupPosition].question!!

    override fun getChild(groupPosition: Int, childPosition: Int): Any = faqList[groupPosition].answer!!

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val listTitle = getGroup(groupPosition) as String
        var cv = convertView
        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = layoutInflater.inflate(R.layout.item_faq, null)
        }
        val ivArrow: ImageView = cv?.findViewById(R.id.arrow)!!
        val background: ConstraintLayout = cv.findViewById(R.id.faqItem)
        val view: View = cv.findViewById(R.id.view)

        if (isExpanded) {
            ivArrow.setImageResource(R.drawable.ic_arrow_up)
            background.setBackgroundResource(R.color.green_50a)
            view.viewGone()
        } else {
            ivArrow.setImageResource(R.drawable.ic_arrow_down)
            background.setBackgroundResource(R.color.app_background)
            view.viewVisible()
        }

        val expandedListTextView = cv.findViewById(R.id.tvQuestionTitle) as TextView
        expandedListTextView.text = listTitle
        return cv
    }

    override fun hasStableIds(): Boolean = false

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val listTitle = getChild(groupPosition, childPosition) as String
        var cv = convertView
        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = layoutInflater.inflate(R.layout.item_faq_child, null)
        }

        val expandedListTextView = cv?.findViewById(R.id.tvAnswer) as TextView
        expandedListTextView.text = listTitle
        return cv
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
}