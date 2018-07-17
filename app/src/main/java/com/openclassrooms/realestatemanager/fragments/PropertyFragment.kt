package com.openclassrooms.realestatemanager.fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dmallcott.dismissibleimageview.DismissibleImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.activities.MainActivity
import com.openclassrooms.realestatemanager.models.Property
import kotlinx.android.synthetic.main.fragment_property.*
import java.text.DateFormat


/**
 * Created by Mickael Hernandez on 09/05/2018.
 */
class PropertyFragment : Fragment() {

    companion object {
        fun newInstance(prop: Property): PropertyFragment {
            val myFragment = PropertyFragment()
            val args = Bundle()
            args.putParcelable("property", prop)
            myFragment.arguments = args
            return myFragment
        }
    }

    private lateinit var dateFormat: DateFormat

    private lateinit var pid: String

    private lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        dateFormat = android.text.format.DateFormat.getDateFormat(context?.applicationContext)
        return inflater.inflate(R.layout.fragment_property, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentView = view
        val prop = arguments?.getParcelable<Property>("property")
        arguments?.remove("property")
        if(prop != null){
            updateUIFromProperty(prop)
            MainActivity.colRef.document(prop.pid).addSnapshotListener { doc, _ ->
                if(doc != null){
                    val property = doc.toObject(Property::class.java)
                    if(property != null){
                        updateUIFromProperty(property)
                    }
                }
            }
        }
        property_overlay.setOnClickListener {
                activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        //card_view.setOnClickListener {  }

        fab_edit.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            (activity as MainActivity).displayFragment(EditPropertyFragment.newInstance(prop!!))
        }
    }

    /** Update UI based on a Property object */
    private fun updateUIFromProperty(prop: Property) {
        if(::fragmentView.isInitialized){
            try {
                // Setting UI elements according to the property object
                fragmentView.findViewById<TextView>(R.id.property_location).text = prop.location
                fragmentView.findViewById<TextView>(R.id.property_type).text = prop.type
                fragmentView.findViewById<TextView>(R.id.property_address).text = prop.address
                fragmentView.findViewById<TextView>(R.id.property_desc).text = prop.description
                fragmentView.findViewById<TextView>(R.id.property_surface).text = getString(R.string.square_meters, prop.surface)
                fragmentView.findViewById<TextView>(R.id.property_rooms).text = prop.roomsCount.toString()
                fragmentView.findViewById<TextView>(R.id.property_price).text = getString(R.string.price_tag, prop.price)
                fragmentView.findViewById<TextView>(R.id.property_entryDate).text = dateFormat.format(prop.entryDate)
                fragmentView.findViewById<TextView>(R.id.property_saleDate).text = dateFormat.format(prop.saleDate)
                fragmentView.findViewById<TextView>(R.id.property_agent).text = prop.agent



                // Status
                val statusView = fragmentView.findViewById<TextView>(R.id.property_status)
                if(prop.status){
                    statusView.text = this.getString(R.string.available)
                    statusView.setTextColor(Color.parseColor("#4caf50"))
                } else {
                    statusView.text = this.getString(R.string.unavailable)
                    statusView.setTextColor(Color.RED)
                }

                // Pictures
                val picturesLayout = fragmentView.findViewById<LinearLayout>(R.id.pictures_layout)
                if(picturesLayout != null){
                    picturesLayout.removeAllViews()
                    for(url in prop.picturesList){
                        val img = DismissibleImageView(context)
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 800)
                        //params.weight = 1.0f
                        //params.gravity = Gravity.FILL
                        params.setMargins(0, 0, 0, 0)
                        //img.layoutParams = params
                        picturesLayout.addView(img)
                        Glide.with(context!!).load(url).into(img)
                    }
                }

                // Map
                val latlng = Utils.getLocationFromAddress(context, prop.address)
                val mapFragment = activity?.supportFragmentManager?.findFragmentById(R.id.property_map) as? SupportMapFragment
                mapFragment?.getMapAsync {
                    it.addMarker(MarkerOptions().position(latlng))
                    it.moveCamera(CameraUpdateFactory.newLatLng(latlng))
                }

            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}