plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.douyinline"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.douyinline"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    
    // Fragment
    implementation("androidx.fragment:fragment:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // 分页加载
    implementation("androidx.paging:paging-runtime:3.2.1")

    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.material:material:1.9.0")

    // 下拉刷新
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("io.github.scwang90:refresh-footer-classics:2.1.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}