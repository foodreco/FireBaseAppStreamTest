package com.dreamreco.firebaseappstreamtest.ui.firestorefts

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dreamreco.firebaseappstreamtest.R
import com.dreamreco.firebaseappstreamtest.databinding.FragmentFTBinding
import com.dreamreco.firebaseappstreamtest.ui.firestorelist.*

class FTSFragment : Fragment() {

    private val viewModel by viewModels<FireStoreListViewModel>()
    private val binding by lazy { FragmentFTBinding.inflate(layoutInflater) }

    private lateinit var searchView: SearchView
    private var priceTextView: TextView? = null
    private var autoComplete: SearchView.SearchAutoComplete? = null

    private var searchText: String? = null
    private var passDrinkInfoData: DrinkInfo? = null


    private var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(newText: String): Boolean {
                return if (newText != "") {
                    val query = newText.trim()
                    // 불러오기 중복 방지 코드
                    if (searchText != query) {
                        searchText = query
                        viewModel.getDrinkInfoLiveData(query)
                            .observe(viewLifecycleOwner) { drinkInfo ->
                                setDrinkInfoBox(drinkInfo)
                            }
                    }
                    true
                } else {
                    Toast.makeText(context, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            //텍스트 입력/수정시에 호출 : 사용안함
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchAutoComplete()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.getProductNameListLiveData().observe(viewLifecycleOwner) { productNameList ->
            Log.e("FTSFragment", "리포지토리 productNameList : $productNameList")
            // 테스트
            autoComplete?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    productNameList
                )
            )
        }
        return binding.root
    }


    @SuppressLint("RestrictedApi")
    private fun setSearchAutoComplete() {
        searchView = binding.searchView
        binding.searchView.setOnQueryTextListener(searchViewTextListener)

//        autoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        //TODO : joodiary 에서는 되고 여기선 안된다 ?
        autoComplete = searchView.findViewById(androidx.constraintlayout.widget.R.id.search_src_text)

        autoComplete?.setDropDownBackgroundResource(android.R.color.white)
        autoComplete?.setBackgroundColor(Color.RED)
        autoComplete?.setTextColor(Color.WHITE)

        // SearchAutoComplete Item 선택 시 발생하는 코드
        autoComplete?.setOnItemClickListener { adapterView, view, itemIndex, id ->
            val query = adapterView.getItemAtPosition(itemIndex) as String
            // 불러오기 중복 방지 코드
            if (searchText != query) {
                searchText = query
                viewModel.getDrinkInfoLiveData(query)
                    .observe(viewLifecycleOwner) { drinkInfo ->
                        setDrinkInfoBox(drinkInfo)
                    }
            }
        }
    }

    private fun setDrinkInfoBox(drinkInfo: DrinkInfo?) {
        if ((drinkInfo?.degree == 100) && (drinkInfo.productName == "연결실패")) {
            passDrinkInfoData = null
            with(binding) {
                drinkInfoCardView.visibility = View.GONE
                setDrinkImageView(null)
            }
        } else {
            passDrinkInfoData = drinkInfo
            with(binding) {
                if (drinkInfo != null) {
                    viewModel.getDrinkInfoImage(drinkInfo.type, drinkInfo.productName)
                        .observe(viewLifecycleOwner) { imageURL ->
                            Log.e("FTS조각", "imageURL : $imageURL")
                            setDrinkImageView(imageURL)
                        }
                    drinkInfoCardView.visibility = View.VISIBLE
                    drinkInfoName.text = "#${drinkInfo.productName}"
                    drinkInfoDegree.text = "${drinkInfo.degree}%"
                    drinkInfoVolume.text = "${drinkInfo.volume}ml"
                } else {
                    drinkInfoCardView.visibility = View.GONE
                    setDrinkImageView(null)
                }
            }
        }
    }

    private fun setDrinkImageView(url: String?) {
        if (url != null) {
            Glide.with(requireContext())
                .load(url)
                .into(binding.drinkInfoImageView)
        } else {
            binding.drinkInfoImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_image
                )
            )
        }
    }
}
