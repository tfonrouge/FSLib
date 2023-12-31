package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.model.base.IUserRole
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.services.getUserPermission
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

abstract class IUserRoleColl<U : IUser<UID>, UID : Any, UR : IUserRole<U, UID>, FILT : IApiFilter>(
    klass: KClass<UR>,
) : Coll<UR, OId<IUserRole<U, UID>>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutineColl.ensureUniqueIndex(
            IUserRole<U, UID>::userId, IUserRole<U, UID>::appRoleId
        )
    }

    @Suppress("unused")
    open suspend fun getPermission(user: U?, kCallable: KCallable<*>): SimpleState {
        return getUserPermission(
            user = user,
            kCallable = kCallable,
            userRoleColl = this
        )
    }
}
