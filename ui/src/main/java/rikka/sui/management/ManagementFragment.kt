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

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filter(newText)
                return true
            }
        })
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
