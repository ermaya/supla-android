object Deps {

    const val Kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.Kotlin}"
    const val Multidex = "androidx.multidex:multidex:${Versions.Multidex}"
    const val AndroidChart = "com.github.PhilJay:MPAndroidChart:${Versions.AndroidChart}"
    const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Coroutines}"
    const val GoogleMaterial = "com.google.android.material:material:${Versions.GoogleMaterial}"
    const val GoogleServices = "com.google.gms:google-services:${Versions.GoogleServices}"

    const val KotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin}"
    const val AndroidBuildTools = "com.android.tools.build:gradle:7.4.2"

    const val Spotless = "com.diffplug.spotless:spotless-plugin-gradle:${Versions.Spotless}"

    const val FragmentViewBinding = "com.github.Zhuinden:fragmentviewbindingdelegate-kt:${Versions.FragmentViewBinding}"

    object Hilt {
        const val Hilt = "com.google.dagger:hilt-android:${Versions.Hilt}"
        const val Kapt = "com.google.dagger:hilt-android-compiler:${Versions.Hilt}"
        const val Classpath = "com.google.dagger:hilt-android-gradle-plugin:${Versions.Hilt}"
    }

    object Androidx {
        const val Fragment = "androidx.fragment:fragment-ktx:${Versions.Androidx.Fragment}"
        const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.Androidx.ConstraintLayout}"
        const val Preferences = "androidx.preference:preference-ktx:${Versions.Androidx.Preferences}"
        const val Worker = "androidx.work:work-runtime-ktx:${Versions.Androidx.Worker}"
        const val RecyclerView = "androidx.recyclerview:recyclerview:${Versions.Androidx.RecyclerView}"

        object Core {
            const val Core = "androidx.core:core:${Versions.Androidx.Core}"
            const val Ktx = "androidx.core:core-ktx:${Versions.Androidx.Core}"
        }

        object Lifecycle {
            const val Runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.Androidx.Lifecycle}"
            const val Viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.Androidx.Lifecycle}"
            const val Livedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.Androidx.Lifecycle}"
            const val Extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.Androidx.LifecycleExtensions}"
        }

        object Navigation {
            const val Fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.Androidx.Navigation}"
            const val UI = "androidx.navigation:navigation-ui-ktx:${Versions.Androidx.Navigation}"
            const val DynamicFeatures = "androidx.navigation:navigation-dynamic-features-fragment:${Versions.Androidx.Navigation}"

            const val SafeArgsGradlePlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.Androidx.Navigation}"
        }

        object Security {
            const val Crypto = "androidx.security:security-crypto:${Versions.Androidx.Security}"
        }

        object Compose {
            const val UI = "androidx.compose.ui:ui:${Versions.Androidx.Compose.Core}"
            const val Material = "androidx.compose.material:material:${Versions.Androidx.Compose.Core}"
            const val Tooling = "androidx.compose.ui:ui-tooling:${Versions.Androidx.Compose.Core}"
            const val ConstraintLayout = "androidx.constraintlayout:constraintlayout-compose:${Versions.Androidx.Compose.ConstaintLayout}"
        }

        object Room {
            const val Runtime = "androidx.room:room-runtime:${Versions.Androidx.Room}"
            const val Compiler = "androidx.room:room-compiler:${Versions.Androidx.Room}"
            const val RxJava = "androidx.room:room-rxjava3:${Versions.Androidx.Room}"
        }
    }

    object RxJava {
        const val RxJava = "io.reactivex.rxjava3:rxjava:${Versions.RxJava.RxJava}"
        const val RxAndroid = "io.reactivex.rxjava3:rxandroid:${Versions.RxJava.RxAndroid}"
        const val RxKotlin = "com.github.reactivex:rxkotlin:${Versions.RxJava.RxKotlin}"
    }

    object Firebase {
        const val Bom = "com.google.firebase:firebase-bom:${Versions.Firebase}"
        const val Messaging = "com.google.firebase:firebase-messaging-ktx"
    }

    object Testing {

        const val Robolectric = "org.robolectric:robolectric:${Versions.Testing.Robolectric}"
        const val JUnit = "junit:junit:${Versions.Testing.JUnit}"
        const val Hamcrest = "org.hamcrest:hamcrest-all:${Versions.Testing.Hamcrest}"
        const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Testing.Coroutines}"
        const val Mockk = "io.mockk:mockk:${Versions.Testing.Mockk}"
        const val AssertJ = "org.assertj:assertj-core:${Versions.Testing.AssertJ}"

        object Mockito {
            const val Core = "org.mockito:mockito-core:${Versions.Testing.Mockito.Core}"
            const val Kotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.Testing.Mockito.Kotlin}"
        }

        object Androidx {
            const val Core = "androidx.test:core:${Versions.Testing.Androidx.Core}"
            const val Navigation = "androidx.navigation:navigation-testing:${Versions.Androidx.Navigation}"
            const val Runner = "androidx.test:runner:${Versions.Testing.Androidx.Runner}"
            const val Rules = "androidx.test:rules:${Versions.Testing.Androidx.Rules}"
            const val JUnitExtension = "androidx.test.ext:junit:${Versions.Testing.Androidx.JUnitExtension}"
            const val ArchCore = "androidx.arch.core:core-testing:${Versions.Testing.Androidx.ArchCore}"
        }
    }
}