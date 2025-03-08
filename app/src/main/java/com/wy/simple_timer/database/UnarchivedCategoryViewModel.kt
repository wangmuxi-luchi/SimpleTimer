package com.wy.simple_timer.database//package com.wy.simple_timer.database
//
//import android.app.Application
//import kotlinx.coroutines.flow.Flow
//@Deprecated(
//    message = "UnarchivedCategoryViewModel 已弃用，请使用 CategoryViewModel 替代。",
//    replaceWith = ReplaceWith("BaseCategoryAdapterRV"),
//    level = DeprecationLevel.ERROR
//)
//class UnarchivedCategoryViewModel(application: Application) : BaseCategoryViewModel(application){
//    override val categories: Flow<List<Category>> = categoryDao.getUnarchivedRootCategoriesOrderedByPosition()
//}