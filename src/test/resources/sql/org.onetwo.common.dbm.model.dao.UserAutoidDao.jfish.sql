

/*****
 * @name: removeByUserName
 * 批量删除
 */
    delete from test_user_autoid 
        where 1=1 
        [#if userName?has_content]
         and user_name like :userName?likeString
        [/#if]
        -->[#if nickName?has_content]
         and nickName like :nickName?likeString
        -->[/#if]

/*****
 * @name: removeByUserNameWithSpace
 * 批量删除
 */
    delete from test_user_autoid 
        where 1=1 
        -- >[#if userName?has_content]
         and user_name like :userName?likeString
        -- >[/#if]
        -- >[#if nickName?has_content]
         and nickName like :nickName?likeString
        -- >[/#if]
         
/*****
 * @name: batchInsert
 * 批量插入     */
    insert 
    into
        test_user_autoid
        (birthday, email, gender, mobile, nick_name, password, status, user_name) 
    values
        (:birthday, :email, :gender, :mobile, :nickName, :password?encrypt, :status.value, :userName)

/*****
 * @name: batchInsert2
 * 多个参数的批量插入
 */
    insert 
    into
        test_user_autoid
        (birthday, email, gender, mobile, nick_name, password, status, user_name) 
--多个参数的批量插入
    values
        (:allBirthday, :email, :gender, :mobile, :nickName, :password?encrypt, :status, :userName)
        
