package com.example.cotacaofacil.presentation.ui.fragment.provider

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.FragmentHomeProviderBinding
import com.example.cotacaofacil.presentation.ui.dialog.CameraGalleryDialog
import com.example.cotacaofacil.presentation.ui.extension.getBitmapFromUri
import com.example.cotacaofacil.presentation.util.BottomNavigationListener
import com.example.cotacaofacil.presentation.viewmodel.provider.home.HomeProviderViewModel
import com.example.cotacaofacil.presentation.viewmodel.provider.home.contract.HomeProviderEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeProviderFragment : Fragment() {
    private val viewModel by viewModel<HomeProviderViewModel>()
    private lateinit var binding: FragmentHomeProviderBinding
    private val cameraGalleryDialog by lazy { CameraGalleryDialog() }

    private var backPressedOnce: Boolean = false
    private var bottomNavigationListener: BottomNavigationListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeProviderBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationListener) {
            bottomNavigationListener = context
        } else {
            throw RuntimeException()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            dispatchTakePictureIntent()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                HomeProviderEvent.AskAgain -> {
                    backPressedOnce = true
                    Toast.makeText(requireContext(), getString(R.string.click_again_to_close), Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ backPressedOnce = false }, 2000)
                }
                HomeProviderEvent.FinishApp -> {
                    requireActivity().finishAffinity()
                }
                HomeProviderEvent.ClickPartner -> {
                    bottomNavigationListener?.onChangeFragmentBottomNavigation(R.id.partnerBuyerFragment)
                }
                is HomeProviderEvent.ErrorLoadInformation -> Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()

                HomeProviderEvent.Logout -> activity?.finish()

                HomeProviderEvent.EditImage -> {
                    cameraGalleryDialog.show(childFragmentManager, "")
                }
                HomeProviderEvent.ShowCamera -> checkCameraPermission()

                HomeProviderEvent.ShowGallery -> checkExternalStoragePermission()
                HomeProviderEvent.ClickCardPrices -> openFragment(R.id.priceProviderFragment)
            }
        }


        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.textViewCorporateName.text = state.nameCorporation
            binding.textViewFantasyName.text = state.nameFantasy
            binding.textViewFone.text = state.fone
            binding.textViewCnpj.text = state.cnpj
            binding.textViewEmail.text = state.email
            binding.textViewNumberOpenPrice.text = state.quantityPricesOpen
            binding.textViewNumberorderOpen.text = state.quantityOrderOpen
            binding.progressBarImageProfile.isVisible = state.loadingImageProfile
            Glide.with(this)
                .load(state.imageProfile)
                .apply(RequestOptions().transform(CircleCrop()))
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewUser)
        }
    }

    private fun openFragment(idFragment: Int) {
        bottomNavigationListener?.onChangeFragmentBottomNavigation(idFragment)
    }

    private fun setupListeners() {
        binding.cardViewPrice.setOnClickListener {
            viewModel.tapOnCardPrices()
        }
        cameraGalleryDialog.optionPhoto = { optionImage ->
            viewModel.tapOnSelectTypePhoto(optionImage)
        }

        binding.cardViewImageUser.setOnClickListener {
            viewModel.tapOnImageProfile()
        }

        binding.textViewLogoff.setOnClickListener {
            viewModel.tapOnLogout()
        }

        binding.constraintLayoutProvider.setOnClickListener {
            viewModel.tapOnPartner()
        }

        binding.constraintLayoutInformationCorporation.setOnClickListener {
            Toast.makeText(requireContext(), "informações pessoais", Toast.LENGTH_SHORT).show()
        }

        binding.arrow.setOnClickListener {
            viewModel.tapOnArrowBack(backPressedOnce)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.tapOnArrowBack(backPressedOnce)
        }
    }

    private fun checkExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    private fun openGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            viewModel.saveImage(imageBitmap)
        }
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                viewModel.saveImage(it.getBitmapFromUri(context = requireContext()))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val REQUEST_EXTERNAL_STORAGE_PERMISSION = 102
        private const val REQUEST_PICK_IMAGE = 2
    }
}