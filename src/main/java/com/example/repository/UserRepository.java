//package com.example.repository;
//
//import org.springframework.data.jdbc.repository.query.Modifying;
//import org.springframework.data.jdbc.repository.query.Query;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.query.Param;
//
//import com.example.domain.kintai.model.MUser;
//
//public interface UserRepository extends JpaRepository<MUser, String> {
//
//	/** ログインユーザー検索 */
//	@Query("select user"
//			+ " from MUser user"
//			+ " where userId = :userId")
//	public MUser findLoginUser(@Param("userId") String userId);
//
//	/** ユーザー更新 */
//	@Modifying
//	@Query("update MUser"
//			+ " set"
//			+ " password = :password"
//			+ " , userName = :userName"
//			+ " where"
//			+ " userId = :userId")
//	public Integer updateUser(@Param("userId") String userId, @Param("password") String password,
//			@Param("userName") String userName);
//
//}
