package com.nvn.imdb.data.di.qualifiers

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
public annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IODispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainImmediateDispatcher