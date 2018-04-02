package connectors.filestore

import com.amazonaws.services.s3.model.{Grantee, Permission}

class FileLocation

case class GrantedPermission(grantee: Grantee, permission: Permission)

case class AwsFileLocation(bucket: String, key: String, acl: Seq[GrantedPermission] = Nil) extends FileLocation
