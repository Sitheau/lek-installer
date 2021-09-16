using System;
using System.IO;

namespace LekInstaller
{
    public static class PathFinder
    {
        public static string FindSteamPath()
        {
            string path;
            string pfPath = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86);

            path = Path.Combine(pfPath, "Steam");
            if (!Directory.Exists(path))
                throw new DirectoryNotFoundException(String.Format("Library not found at \"{0}\".", path));

            return path;
        }

        public static string[] FindLibraryPaths()
        {
            return null;
        }
    }
}
