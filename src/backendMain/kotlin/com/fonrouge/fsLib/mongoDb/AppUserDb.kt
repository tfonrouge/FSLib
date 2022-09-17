package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppUser
import com.mongodb.client.model.IndexOptions
import kotlinx.coroutines.runBlocking

var AppUserDb: CTableDb<AppUser, String> = object : CTableDb<AppUser, String>(
    klass = AppUser::class
) {
    init {
        runBlocking {
            coroutineColl.ensureUniqueIndex(
                AppUser::code,
                indexOptions = IndexOptions()
                    .collation(collation())
            )
        }
    }
}
