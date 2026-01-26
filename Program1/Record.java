/*
    Class: Record
    Author: Ventura Abram
    The fields listed in order here reflects the order that they're listed in the csv file.
    Fields: self-explanatory, and as described above.
    Methods: all are setters for the fields.  Nothing else.
    */
    public class Record {
        private int DatasetSeqID;
        private String DataEntry;
        private String CaveDataSeries;
        private String BiogRealm;
        private String Continent;
        private String BiomeClass;
        private String Country;
        private String CaveSite;
        private double Lattitude;
        private double Longitude;
        private String SpeciesName;

        public void setDatasetSeqID(int DatasetSeqID) {this.DatasetSeqID = DatasetSeqID; return;}
        public void setDataEntry(String DataEntry) {this.DataEntry = DataEntry; return;}
        public void setCaveDataSeries(String CaveDataSeries) {this.CaveDataSeries = CaveDataSeries; return;}
        public void setBiogRealm(String BiogRealm) {this.BiogRealm = BiogRealm; return;}
        public void setContinent(String Continent) {this.Continent = Continent; return;}
        public void setBiomeClass(String BiomeClass) {this.BiomeClass = BiomeClass; return;}
        public void setCountry(String Country) {this.Country = Country; return;}
        public void setCaveSite(String CaveSite) {this.CaveSite = CaveSite; return;}
        public void setLattitude(double Lattitude) {this.Lattitude = Lattitude; return;}
        public void setLongitude(double Longitude) {this.Longitude = Longitude; return;}
        public void setSpeciesName(String SpeciesName) {this.SpeciesName = SpeciesName; return;}

        public int getDatasetSeqID() {return this.DatasetSeqID;}
        public String getDataEntry() {return this.DataEntry;}
        public String getCaveDataSeries() {return this.CaveDataSeries;}
        public String getBiogRealm() {return this.BiogRealm;}
        public String getContinent() {return this.Continent;}
        public String getBiomeClass() {return this.BiomeClass;}
        public String getCountry() {return this.Country;}
        public String getCaveSite() {return this.CaveSite;}
        public double getLattitude() {return this.Lattitude;}
        public double getLongitude() {return this.Longitude;}
        public String getSpeciesName() {return this.SpeciesName;}



        public boolean equals(Object o) {
            Record other = null;
            if(o instanceof Record) {
                other = (Record) o;
                return this.CaveSite.equals(other.CaveSite);
            } else {
                return false;
            }
        }

        public int compareTo(Record other) {
            if(Double.compare(this.Lattitude, other.Lattitude) < 0) return -1;
            if(Double.compare(this.Lattitude, other.Lattitude) == 0) return 0;
            return 1; 
        }
    }