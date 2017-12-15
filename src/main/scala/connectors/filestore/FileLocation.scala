package connectors.filestore

class FileLocation

case class AwsFileLocation(bucket: String, key: String) extends FileLocation
