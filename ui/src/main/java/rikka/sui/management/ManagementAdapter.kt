/*
 * This file is part of Sui.
 *
 * Sui is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sui is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sui.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2021 Sui Contributors
 */
package rikka.sui.management

import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.Locale
import me.zhanghai.android.fastscroll.PopupTextProvider
import rikka.core.res.resolveColor
import rikka.core.res.resolveColorStateList
import rikka.html.text.toHtml
import rikka.recyclerview.BaseRecyclerViewAdapter
import rikka.recyclerview.ClassCreatorPool
import rikka.sui.R
import rikka.sui.model.AppInfo
import rikka.sui.server.SuiConfig
class ManagementAdapter(context: Context) : BaseRecyclerViewAdapter<ClassCreatorPool>(), PopupTextProvider {

    init {
        creatorPool.putRule(AppInfo::class.java, ManagementAppItemViewHolder.newCreator(createOptionsAdapter(context)))
        setHasStableIds(true)
    }

    private fun createOptionsAdapter(context: Context): ArrayAdapter<CharSequence> {
        val theme = context.theme
        val isNight = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0
        val colorAccent = theme.resolveColor(androidx.appcompat.R.attr.colorAccent)
        val colorForeground = theme.resolveColor(android.R.attr.colorForeground)
        val textColorTertiary = theme.resolveColorStateList(android.R.attr.textColorTertiary)
        val colorError = if (isNight) 0xFF8A80 else 0xFF5252

        val adapter = object : ArrayAdapter<CharSequence>(
            context,
            android.R.layout.simple_spinner_item,
            arrayOf(
                String.format("<font face=\"sans-serif-medium\" color=\"#%2\$s\">%1\$s</font>", context.getString(R.string.permission_allowed), String.format(Locale.ENGLISH, "%06x", colorAccent and 0xffffff)).toHtml(),
                String.format("<font face=\"sans-serif-medium\" color=\"#%2\$s\">%1\$s</font>", context.getString(R.string.permission_denied), String.format(Locale.ENGLISH, "%06x", colorError and 0xffffff)).toHtml(),
                String.format("<font face=\"sans-serif-medium\" color=\"#%2\$s\">%1\$s</font>", context.getString(R.string.permission_hidden), String.format(Locale.ENGLISH, "%06x", colorForeground and 0xffffff)).toHtml(),
                context.getString(R.string.permission_ask)
            )
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1))?.let {
                    it.setTextColor(textColorTertiary)
                    it.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    override fun getItemId(position: Int): Long {
        return getItemAt<Any>(position).hashCode().toLong()
    }

    override fun onCreateCreatorPool(): ClassCreatorPool {
        return ClassCreatorPool()
    }

    fun updateData(data: List<AppInfo>) {
        getItems<Any>().clear()
        getItems<Any>().addAll(data)
        notifyDataSetChanged()
    }

    override fun getPopupText(view: View, position: Int): CharSequence {
        return try {
            val appInfo = getItemAt<AppInfo>(position)
            val appName = appInfo.label

            if (appName.isNullOrEmpty()) {
                " "
            } else {
                appName.substring(0, 1).uppercase()
            }
        } catch (e: Exception) {
            ""
        }
    }
}