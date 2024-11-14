package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

/**
 * Interface representing a user group in the application.
 *
 * @param U The type of user extending from `IUser`.
 * @param UID The type of the user identifier.
 * @param GOU The type of group of user extending from `IGroupOfUser`.
 * @param GR The type of role in group extending from `IRoleInGroup`.
 */
interface IUserGroup<U : IUser<out UID>, UID : Any, GOU : IGroupOfUser<*>, GR : IRoleInGroup<*, GOU>> :
    BaseDoc<OId<IUserGroup<U, UID, GOU, GR>>> {
    override val _id: OId<IUserGroup<U, UID, GOU, GR>>
    val userId: UID
    val groupOfUserId: OId<GOU>
    var roleInGroups: List<GR>
}
