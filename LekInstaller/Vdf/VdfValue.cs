namespace LekInstaller.Vdf
{
    public enum ValueKind
    {
        String,
        Object
    }

    public class VdfValue
    {

        public VdfValue(string data)
        {
            Kind = ValueKind.String;
        }

        public VdfValue(VdfObject data)
        {
            Kind = ValueKind.Object;
        }

        public string GetString()
        {
            return null;
        }

        public VdfObject GetObject()
        {
            return null;
        }

        public ValueKind Kind { get; private set; }
    }
}
