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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.lifecycle.Resource
import rikka.lifecycle.Status
import rikka.sui.model.AppInfo
import rikka.sui.util.AppInfoComparator
import rikka.sui.util.BridgeServiceClient

class ManagementViewModel : ViewModel() {

    private val UI_DEBUG_MODE = true
    private val fullList = ArrayList<AppInfo>()

    val appList = MutableLiveData<Resource<List<AppInfo>>>(null)

    private fun handleList() {
        val list = fullList.sortedWith(AppInfoComparator()).toList()

        appList.postValue(Resource.success(list))
    }

    fun invalidateList() {
        if (appList.value?.status != Status.SUCCESS) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            handleList()
        }
    }
    fun filter(query: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isNullOrBlank()) {
                handleList()
                return@launch
            }
            val filteredList = fullList.filter { appInfo ->
                val appName = appInfo.label ?: ""
                val packageName = appInfo.packageInfo.packageName ?: ""

                appName.contains(query, ignoreCase = true) || packageName.contains(query, ignoreCase = true)
            }
            appList.postValue(Resource.success(filteredList))
        }
    }
    fun reload(context: Context) {
        appList.postValue(Resource.loading(null))

        if (UI_DEBUG_MODE) {
            viewModelScope.launch(Dispatchers.IO) {
                val fakeData = createFakeAppList()
                fullList.clear()
                fullList.addAll(fakeData)
                handleList()
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val result = BridgeServiceClient.getApplications(-1 /* ALL */).apply {
                    forEach { it.label = it.packageInfo.applicationInfo!!.loadLabel(pm) }
                }

                fullList.clear()
                fullList.addAll(result)

                handleList()
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                android.util.Log.e("SuiViewModelFinal", "THE SMOKING GUN! The final error is:", e)
                appList.postValue(Resource.error(e, null))
            }
        }
    }

    private fun createFakeAppList(): List<AppInfo> {
        val names = listOf(
            "Ciallo", "0721", "0d00", "小潼", "xiaotong",
            "测试QQ", "91av", "糖心vlog", "禁漫天堂", "哔咔",
            "AcFun(伪)", "伪萌娘百科", "假时钟", "测试新闻阅读器", "虚拟地图",
            "伪联系人", "虚构记事本", "测试QQ浏览器", "假计算器", "虚拟音乐盒"
        )

        return names.mapIndexed { index, name ->
            val pkg = "com.example.fakeapp${index + 1}"
            AppInfo().apply {
                packageInfo = PackageInfo().apply {
                    packageName = pkg
                    applicationInfo = ApplicationInfo().apply {
                        nonLocalizedLabel = name
                        this.packageName = pkg
                    }
                }
                label = name
                flags = 0
            }
        }
    }
}
