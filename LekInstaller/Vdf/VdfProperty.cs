namespace LekInstaller.Vdf
{
    public class VdfProperty
    {
        public VdfProperty(string label, VdfValue value)
        {
            Label = label;
            Value = value;
        }

        public string Label { get; set; }

        public VdfValue Value { get; set; }
    }
}
