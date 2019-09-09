
/*****
 * @name: findByUserNameLike
 * */
    select
        *
    from
        TEST_USER u
    where
        u.user_name like :userName?likeString
        
        
/****
 * @name: findByUserStatus
 */
    select
        *
    from
        TEST_USER u
    where
        u.status = :status

        