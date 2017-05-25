package com.github.rerorero.oleoleflake.gen;

abstract public class AbstractIdGenBuilder<Selector, Gen> {

    protected abstract class FieldBuilder<Builder extends FieldBuilder<Builder>> {
        protected final int start;
        protected final int size;
        protected boolean invert = false;

        public FieldBuilder(int start, int size, AbstractIdGenBuilder parent) {
            this.start = start;
            this.size = size;
        }

        public Builder invert() {
            invert = true;
            return (Builder)this;
        }

        public Builder flip() {
            return invert();
        }

        abstract protected void setup();

        public Selector nextBit(int size) {
            setup();
            return nextBit(size);
        }

        public Gen build() {
            setup();
            return AbstractIdGenBuilder.build();
        }
    }

    abstract public Selector nextBit(int size);
    abstract public Gen build();
}
