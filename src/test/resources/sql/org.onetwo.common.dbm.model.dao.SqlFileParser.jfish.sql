
/****
 * @name: findUsers
 */
    select
        *
    from
        TEST_USER u
    -- >[@str insertPrefix='where' trimPrefixs='and | or' trimSuffixs='and | or']
        [#if query.userName?has_content]
            u.user_name = :query.userName
        [/#if]
        [#if query.age??]
            and u.age = :query.age
        [/#if]
        [#if query.status??]
            and u.status = :query.status or 
        [/#if]
    -- >[/@str]

/***
 * @name: updateUsers
 */
    update
        TEST_USER 
    -- >[@str insertPrefix='set' trimSuffixs=',']
        [#if query.userName?has_content]
            user_name = :query.userName, 
        [/#if]
        [#if query.age??]
            age = :query.age, 
        [/#if]
        [#if query.status??]
            status = :query.status,
        [/#if]
    -- >[/@str]
    where 
        id = :query.id

/****
 * @name: findUsersWithWhere
 */
    select
        *
    from
        TEST_USER u
    [@where]
        [#if query.userName?has_content]
            u.user_name = :query.userName
        [/#if]
        [#if query.age??]
            and u.age = :query.age
        [/#if]
        [#if query.status??]
            and u.status = :query.status or 
        [/#if]
    [/@where]
        
/***
 * @name: updateUsersWithSet
 */
    update
        TEST_USER 
    -- >[@set]
        [#if query.userName?has_content]
            user_name = :query.userName, 
        [/#if]
        [#if query.age??]
            age = :query.age, 
        [/#if]
        [#if query.status??]
            status = :query.status,
        [/#if]
    -- >[/@set]
    where 
        id = :query.id
