package com.example.woodward.owen.iden.protect.di

import com.example.woodward.owen.iden.protect.encryption.RsaKeyStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object WrapperClassModule {
    @Provides
    @FragmentScoped
    fun getWrapperClass() = RsaKeyStore()
}