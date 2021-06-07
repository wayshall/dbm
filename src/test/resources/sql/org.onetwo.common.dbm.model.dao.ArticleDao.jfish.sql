/***
 * @name: dropArtilceForeignKey
 */
ALTER TABLE `test_article` DROP FOREIGN KEY `fk_art_author_user_id`;

/***
 * @name: addArtilceForeignKey
 */
ALTER TABLE `jormtest`.`test_article` 
ADD CONSTRAINT `fk_art_author_user_id` FOREIGN KEY (`author_user_id`) REFERENCES `jormtest`.`test_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
