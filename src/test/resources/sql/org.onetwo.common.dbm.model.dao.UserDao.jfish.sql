
/*****
 * @name: findByUserNameLike
 * */
    select
        *
    from
        TEST_USER u
    where
        u.user_name like :userName?likeString