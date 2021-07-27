package com.example.travelencer_android_2021

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelencer_android_2021.adapter.AddPlaceSearchResultAdapter
import com.example.travelencer_android_2021.api.KakaoLocalApi
import com.example.travelencer_android_2021.api.KakaoLocalApiRetrofitClient
import com.example.travelencer_android_2021.databinding.ActivityAddPlaceSearchAddressBinding
import com.example.travelencer_android_2021.model.ModelAddressSearchList
import com.example.travelencer_android_2021.model.ModelKakaoLocalApi
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response


class AddPlaceSearchAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlaceSearchAddressBinding
    private val codeAddress = "address"
    private val page = 1 //임시값
    private val size = 30 //최대값

    // Kakao Local Api 데이터 받아오기
    private val kakaoApi = KakaoLocalApiRetrofitClient.apiService


    fun callKakaoLocalKeyword(address: String): ArrayList<ModelAddressSearchList> {
        val kakao = MutableLiveData<ModelKakaoLocalApi>()
        var addressList: ArrayList<ModelAddressSearchList> = arrayListOf()

        kakaoApi.getKakaoAddress(KakaoLocalApi.API_KEY, address = address, size = size)
            .enqueue(object : retrofit2.Callback<ModelKakaoLocalApi> {
                override fun onResponse(
                        call: Call<ModelKakaoLocalApi>,
                        response: Response<ModelKakaoLocalApi>
                ) {
                    kakao.value = response.body()

                    //<<<에러 시 에러코드 받아오는 부분>>>
                    if (kakao.value == null) {
                        Log.i("오류 kakao.value == null", response.errorBody().toString())
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                Log.i("오류 try", jObjError.getJSONObject("error").getString("message"))
                                //Toast.makeText(getContext(), jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.i("오류 catch", e.message.toString())
                                //Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    if (!response.isSuccessful) {
                        Log.i("!response.isSuccessful", response.errorBody().toString())
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            Log.i("오류 try", jObjError.getJSONObject("error").getString("message"))
                            //Toast.makeText(getContext(), jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.i("오류 catch", e.message.toString())
                            //Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    //<<<여기까지>>>

//                    Log.i("kakao", "0 ${kakao.value}")
                    Log.i("kakao", "0 ${kakao.value!!.documents[0].address_name}")
//                    Log.i("kakao", "1 ${kakao.value!!.documents[1].address_name}")
//                    Log.i("kakao", "2 ${kakao.value!!.documents[2].address_name}")
//                    Log.i("kakao", "0 ${kakao.value!!.documents[0].address_type}")
//                    Log.i("kakao", "${kakao.value!!.documents[0].x}")
//                    Log.i("kakao", "${kakao.value!!.documents[0].y}")
//                    Log.i("kakao", "${kakao.value!!.documents[0].address.region_1depth_name}")
//                    Log.i("kakao", "${kakao.value!!.documents[0].address.region_2depth_name}")
//                    Log.i("kakao", "${kakao.value!!.documents[0].address.region_3depth_name}")

                    Log.i("kakao", "total = ${kakao.value!!.meta.total_count}")
                    Log.i("kakao", "size = ${kakao.value!!.documents.size}")

                    // 검색결과 총 개수
                    val total = kakao.value?.let { it.meta.total_count }

                    // TODO : 일단 검색결과 첫 페이지만 나오게 만듦..
                    if (total != null) {
                        val loop = if (total < size) total else size

                        //document: address_name통일... 아니면 documents: address: 나 documets: road_address:
                        //좌표정보나 DB에 넘길 정보들 더 챙기기
/*                        for (i in 0 until loop) {
                            val name = kakao.value!!.documents[i].address_name
                            addressList.add(ModelAddressSearchList(name))
                        }*/
                        for (element in kakao.value!!.documents) {
                            val name = element.address_name
                            addressList.add(ModelAddressSearchList(name))
                        }
                    }
                }

                override fun onFailure(call: Call<ModelKakaoLocalApi>, t: Throwable) {
                    Log.e("실패 로그", "실패")
                    t.printStackTrace()
                }
            })

        return addressList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceSearchAddressBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 리사이클러 뷰 설정, 어댑터에 데이터 담아 연결
        binding.rvAddressSearchResultList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvAddressSearchResultList.setHasFixedSize(true)

        binding.btnAddressSearch.setOnClickListener {
            val addressList = callKakaoLocalKeyword(binding.etAddressSearchKeyword.text.toString())

            //callKakaoLocalKeyword("테헤란로")
            val mAdapter = AddPlaceSearchResultAdapter(addressList)
            binding.rvAddressSearchResultList.adapter = mAdapter
            mAdapter.notifyDataSetChanged()

            //키보드 내림
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }

        // TODO : launch함수 만들고 리사이클러뷰 어댑터에서 finish()담긴 리스너 달기 -> 확인 창 한번 띄우면 좋겠다


    }
}