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

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import rikka.lifecycle.Resource
import rikka.lifecycle.Status
import rikka.lifecycle.viewModels
import rikka.recyclerview.fixEdgeEffect
import rikka.sui.R
import rikka.sui.app.AppFragment
import rikka.sui.databinding.ManagementBinding
import rikka.sui.model.AppInfo

class ManagementFragment : AppFragment() {

    private var _binding: ManagementBinding? = null
    private val binding: ManagementBinding get() = _binding!!

    private val viewModel by viewModels { ManagementViewModel() }
    private val adapter by lazy { ManagementAdapter(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val context = view.context
        view.post {
            val parentView = view.parent as? ViewGroup
            if (parentView != null) {
                val hostPaddingLeft = parentView.paddingLeft
                val hostPaddingRight = parentView.paddingRight
                if (hostPaddingLeft > 0 || hostPaddingRight > 0) {
                    val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                    if (layoutParams != null) {
                        layoutParams.leftMargin = -hostPaddingLeft
                        layoutParams.rightMargin = -hostPaddingRight
                        view.layoutParams = layoutParams
                    }
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.list.setPadding(
                binding.list.paddingLeft,
                0,
                binding.list.paddingRight,
                systemBars.bottom
            )

            insets
        }
        binding.list.apply {
//            borderVisibilityChangedListener =
//                OnBorderVisibilityChangedListener { top: Boolean, _: Boolean, _: Boolean, _: Boolean ->
//                    appActivity?.appBar?.setRaised(!top)
//                }
            adapter = this@ManagementFragment.adapter
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            fixEdgeEffect()
            this.setItemViewCacheSize(20)
            this.recycledViewPool.setMaxRecycledViews(0, 20)
            me.zhanghai.android.fastscroll.FastScrollerBuilder(this)
                .useMd2Style()
                .build()

            layoutAnimationListener = object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            }
        }

        binding.swipeRefresh.apply {
            setOnRefreshListener {
                viewModel.reload(context)
            }
            val typedValue = TypedValue()

            context.theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true)
            val colorAccent = typedValue.data
            setColorSchemeColors(colorAccent)

            context.theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true)
            val actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
            setProgressViewOffset(false, actionBarSize, (64 * resources.displayMetrics.density + actionBarSize).toInt())
        }

        viewModel.appList.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> onLoading()
                Status.SUCCESS -> onSuccess(it)
                Status.ERROR -> onError(it.error)
                else -> {}
            }
        }
        if (savedInstanceState == null) {
            viewModel.reload(requireContext())
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.management_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        var isSearchViewInitialized = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isSearchViewInitialized && newText.isNullOrEmpty()) {
                    isSearchViewInitialized = true
                    return true
                }
                viewModel.filter(newText)
                return true
            }
        })
        val overflowItem = menu.findItem(R.id.action_overflow)
        requireActivity().findViewById<View>(R.id.toolbar)?.post {
            val overflowButtonView = requireActivity().findViewById<View>(R.id.action_overflow)
            if (overflowButtonView != null) {
                overflowButtonView.setOnClickListener { anchorView ->
                    showOverflowPopupMenu(anchorView)
                }
            } else {
                overflowItem.setOnMenuItemClickListener {
                    showOverflowPopupMenu(requireActivity().findViewById(R.id.toolbar))
                    true
                }
            }
        }
    }
    private fun showOverflowPopupMenu(anchorView: View) {
        val popupMenu = androidx.appcompat.widget.PopupMenu(requireContext(), anchorView)
        popupMenu.inflate(R.menu.overflow_popup_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_shortcut -> {
                    try {
                        rikka.sui.util.BridgeServiceClient.requestPinnedShortcut()
                        android.widget.Toast.makeText(requireContext(), "在尝试创建喵...", android.widget.Toast.LENGTH_SHORT).show()

                    } catch (e: Throwable) {
                        android.util.Log.e("SuiShortcutRPC", "Failed to request pinned shortcut via RPC", e)
                        android.widget.Toast.makeText(requireContext(), "创建失败喵: " + e.message, android.widget.Toast.LENGTH_LONG).show()
                    }
                    true
                }
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun resolveThemeColor(@androidx.annotation.AttrRes attrRes: Int): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    private fun showAboutDialog() {
        val versionName = try {
            rikka.sui.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "Unknown"
        }
        val message = android.text.SpannableStringBuilder().apply {
            val title = "Sui\n"
            append(title)
            val typedValue = android.util.TypedValue()
            requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            setSpan(android.text.style.RelativeSizeSpan(1.2f), 0, title.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, title.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(android.text.style.ForegroundColorSpan(typedValue.data), 0, title.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("v$versionName\n\n")
            append("本项目遵循 GPLv3 在 ")
            val startGithub = length
            append("GitHub")
            setSpan(android.text.style.URLSpan("https://github.com/XiaoTong6666/Sui"), startGithub, length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(" 开源 \nCopyright (c) 2021-2025 Sui Contributors\n\n")
            append("贡献者: Rikka, yujincheng08, Kr328, yangFenTuoZi, XiaoTong")
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.about_button_ok, null)
            .show()
            .findViewById<android.widget.TextView>(android.R.id.message)
            ?.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onLoading() {
        binding.progress.isVisible = true
        binding.list.isGone = true

        binding.swipeRefresh.isRefreshing = false
        binding.swipeRefresh.isEnabled = false

        adapter.updateData(emptyList())
    }

    private fun onError(e: Throwable) {
        binding.progress.isGone = true
        binding.list.isVisible = true

        binding.swipeRefresh.isRefreshing = false
        binding.swipeRefresh.isEnabled = true
    }

    private fun onSuccess(data: Resource<List<AppInfo>?>) {
        binding.progress.isGone = true
        binding.list.isVisible = true

        binding.swipeRefresh.isRefreshing = false
        binding.swipeRefresh.isEnabled = true

        data.data?.let {
            adapter.updateData(it)

            if (it.isNotEmpty()) {
                binding.list.scheduleLayoutAnimation()
            }
        }
    }
}
