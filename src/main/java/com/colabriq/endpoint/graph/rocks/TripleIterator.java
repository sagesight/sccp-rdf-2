package com.colabriq.endpoint.graph.rocks;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.colabriq.endpoint.graph.base.store.iterator.functional.ConcatExtendedIterator;
import com.colabriq.endpoint.graph.base.store.iterator.functional.FilteredExtendedIterator;
import com.colabriq.endpoint.graph.base.store.iterator.functional.MappedExtendedIterator;
import com.colabriq.endpoint.graph.base.store.iterator.functional.WrappedExtendedIterator;
import com.colabriq.rocks.PrefixIterator;
import com.colabriq.shared.encode.RDFBinary;

/**
 * Interfaces {@link ExtendedIterator} with {@link PrefixIterator}
 * Recheck each Triple against the pattern (just in case there's type weirdness...)
 */
public class TripleIterator implements ExtendedIterator<Triple>, AutoCloseable {
	private final PrefixIterator it;
	
	public TripleIterator(PrefixIterator it) {
		this.it = it;	
	}
	
	@Override
	public void close() {
		it.close();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Triple next() {
		return RDFBinary.decodeTriple(it.next().val);
	}
	
	@Override
	public Triple removeNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <X extends Triple> ExtendedIterator<Triple> andThen(Iterator<X> other) {
		return new ConcatExtendedIterator<>(this, new WrappedExtendedIterator<>(other));
	}

	@Override
	public ExtendedIterator<Triple> filterKeep(Predicate<Triple> f) {
		return new FilteredExtendedIterator<>(this, f);
	}

	@Override
	public ExtendedIterator<Triple> filterDrop(Predicate<Triple> f) {
		return new FilteredExtendedIterator<>(this, x -> !f.test(x));
	}

	@Override
	public <Y> ExtendedIterator<Y> mapWith(Function<Triple, Y> mapper) {
		return new MappedExtendedIterator<>(this, mapper);
	}

	@Override
	public List<Triple> toList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Triple> toSet() {
		throw new UnsupportedOperationException();
	}
}
