using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LekInstaller
{
    class Program
    {
        static void Main()
        {
            Console.WriteLine(PathFinder.FindSteamLibrary());
            Console.ReadLine();
        }
    }
}
