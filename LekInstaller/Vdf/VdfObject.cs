using System;
using System.Collections.Generic;

namespace LekInstaller.Vdf
{
    public class VdfObject
    {
        private Dictionary<string, VdfProperty> properties;

        public VdfObject(Dictionary<string, VdfProperty> properties)
        {
            Properties = properties;
        }

        Dictionary<string, VdfProperty> Properties
        {
            get { return properties; }
            set
            {
                if (value == null)
                    throw new ArgumentNullException();
                properties = value;
            }
        }
    }
}
