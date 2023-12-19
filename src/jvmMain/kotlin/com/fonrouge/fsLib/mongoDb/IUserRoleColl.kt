package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.model.base.IUserRole
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

abstract class IUserRoleColl<U : IUser<UID>, UID : Any, UR : IUserRole<U, UID>, FILT : ApiFilter>(
    klass: KClass<UR>,
) : Coll<UR, OId<IUserRole<U, UID>>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutineColl.ensureUniqueIndex(
            IUserRole<U, UID>::userId, IUserRole<U, UID>::appRoleId
        )
    }
}
