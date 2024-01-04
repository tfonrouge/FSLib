package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.model.base.IGroupRole
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.model.base.IUserGroup
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IUserGroupColl<U : IUser<UID>, UID : Any, UG : IUserGroup<U, UID, GOU, GR>, GOU : IGroupOfUser<*>, GR : IGroupRole<*, GOU>, FILT : IApiFilter>(
    klass: KClass<UG>,
) : Coll<UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>(
    klass = klass,
    debug = true
) {
    override suspend fun CoroutineCollection<UG>.ensureIndexes() {
        ensureUniqueIndex(IUserGroup<U, UID, GOU, *>::userId, IUserGroup<U, UID, GOU, *>::groupOfUserId)
    }
}
