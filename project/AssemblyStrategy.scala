import sbt.Def
import sbtassembly.AssemblyKeys.assembly
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy
import sbtassembly.{MergeStrategy, PathList}

object AssemblyStrategy {
  def apply(): Def.SettingsDefinition = assemblyMergeStrategy in assembly := {
    case PathList(ps@_*) if ps.last == "Log4j2Plugins.dat" => Log4j2MergeStrategy.plugincache
    case x =>
      val oldStrategy: String => MergeStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
}
