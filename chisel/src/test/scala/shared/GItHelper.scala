package shared

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

object GItHelper {
  val repo: Repository =
    new FileRepositoryBuilder().setGitDir(new File("../.git")).readEnvironment().findGitDir().setMustExist(true).build()
  val git        = new Git(repo)
  val date       = new Date(git.log().call().iterator().next().getCommitTime.toLong * 1000)
  val pattern    = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  val format     = new SimpleDateFormat(pattern)
  val dateString = format.format(date)
}
