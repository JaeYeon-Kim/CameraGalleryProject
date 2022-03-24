package com.kjy.cameraandgallery

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.kjy.cameraandgallery.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    // 사진정보를 담을 변수를 프로퍼티로 설정
    var photoUri:Uri? = null

    // 실제 촬영 이미지를 외부 저장소에 저장하기 때문에 카메라 권한과 함께 외부 저장소 권한 처리 런처를 저장해 둘 변수 2개 선언
    // 추가로 카메라 요청 런처도 선언
    // 앞선 두개는 Launcher의 Contract로 RequestPermission을 사용하기 때문에 제네릭 형태가 <String>
    // CameraLauncher는 TakePicture를 사용하기 때문에 제네릭으로 <Uri>를 사용함.
    lateinit var cameraPermission: ActivityResultLauncher<String> // 카메라 권한
    lateinit var storagePermission: ActivityResultLauncher<String>   // 저장소 권한
    lateinit var cameraLauncher: ActivityResultLauncher<Uri>    // 카메라 앱 호출
    lateinit var galleryLauncher: ActivityResultLauncher<String>    // 갤러리 호출

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 카메라 storagePermission 과 cameraPermission 작성
        // 권한 요청이 정상적으로 승인되었으면 setViews()를 호출해서 화면을 시작함.
        storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            if(isGranted) {
                setViews()
            } else {
                Toast.makeText(baseContext, "외부 저장소 권한을 승인해야 앱을 사용할 수 있습니다. ",
                        Toast.LENGTH_LONG).show()
                finish()
            }
        }

        cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(baseContext, "카메라 권한을 승인해야 카메라를 사용할 수 있습니다.",
                        Toast.LENGTH_LONG).show()
                        finish()
            }
        }

        // 카메라 런처 작성 사진 촬영을 위한 TakePicture() 사용
        // 런처의 결과값으로 사진 촬영이 정상일 경우 true가, 오류가 있을 경우 false가 넘어오기 때문에 isSuccess를 별칭으로 사용하면
        // 코딩 시 알아보기가 쉬움.
        // TakePicture의 결과는 Boolean타입 이므로 실제 사진 정보를 얻을 수 없어서 사진정보를 담을 변수를 프로퍼티로 설정 해야함.
        cameraLauncher = registerForActivityResult(ActivityResultContracts.
        TakePicture()) { isSuccess ->
            // isSuccess가 true일 때 photoUri를 화면에 셋팅하는 코드를 작성.
            if(isSuccess) {
                binding.imagePreview.setImageURI(photoUri) }
        }
        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)


        // 갤러리 런처 설정
        // 카메라와 다르게 Contract로 GetContent()를 사용 프로퍼티 지정 메서드 String 사용
        galleryLauncher = registerForActivityResult(ActivityResultContracts.
        GetContent()) { uri ->
            binding.imagePreview.setImageURI(uri)
        }

    }

    // 외부 저장소 권한이 승인되었을 때 호출할 setViews() 메서드를 만들어줌.
    // 버튼 클릭시 카메라 권한을 요청하는 코드 추가
    fun setViews() {
        binding.buttonCamera.setOnClickListener {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }

        // 갤러리 버튼 클릭 이벤트
        binding.buttonGallery.setOnClickListener {
            openGallery()

        }
    }

    // 카메라를 요청하는 메서드
    // 사진 촬영후 저장할 임시 파일을 생성하고 변수에 담아둠.
    fun openCamera() {
        val photoFile = File.createTempFile(
            "IMG_",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        )
        // 앞에서 생성한 파일의 Uri를 생성해서 photoUri 변수에 담고 launch() 메서드에 전달해서 카메라 호출.
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )

        cameraLauncher.launch(photoUri)
    }

    // openGallery 메서드(갤러리 호출) launch로 모든 종류의 이미지를 불러올 수 있도록 "image/*"
    fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}