import React, { useState } from 'react';
import { Search, Filter, X } from 'lucide-react';

const CATEGORIES = ['MOVIE','CONCERT','SPORTS','TRAVEL','THEATER','CONFERENCE','OTHER'];

export default function EventFilter({ onFilter, onSearch }) {
  const [keyword,   setKeyword]   = useState('');
  const [category,  setCategory]  = useState('');
  const [location,  setLocation]  = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate,   setEndDate]   = useState('');
  const [showAdv,   setShowAdv]   = useState(false);

  const hasFilters = category || location || startDate || endDate;

  const handleSearch = (e) => {
    e.preventDefault();
    if (keyword.trim()) {
      onSearch(keyword.trim());
    } else {
      applyFilter();
    }
  };

  const applyFilter = () => {
    onFilter({
      category:  category  || undefined,
      location:  location  || undefined,
      startDate: startDate ? new Date(startDate).toISOString() : undefined,
      endDate:   endDate   ? new Date(endDate).toISOString()   : undefined,
    });
  };

  const clearAll = () => {
    setKeyword(''); setCategory(''); setLocation('');
    setStartDate(''); setEndDate('');
    onFilter({});
  };

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 mb-6">
      {/* Search bar */}
      <form onSubmit={handleSearch} className="flex gap-2 mb-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search events, venues, artists…"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            className="input pl-10"
          />
        </div>
        <button type="submit" className="btn-primary px-5 text-sm">Search</button>
        <button
          type="button"
          onClick={() => setShowAdv(!showAdv)}
          className={`btn-secondary text-sm flex items-center gap-1.5 ${hasFilters ? 'border-blue-500 text-blue-600' : ''}`}
        >
          <Filter className="w-4 h-4" />
          <span className="hidden sm:inline">Filters</span>
          {hasFilters && <span className="bg-blue-600 text-white rounded-full w-4 h-4 text-xs flex items-center justify-center">!</span>}
        </button>
      </form>

      {/* Advanced filters */}
      {showAdv && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 pt-3 border-t border-gray-100">
          <div>
            <label className="label text-xs">Category</label>
            <select value={category} onChange={e => setCategory(e.target.value)} className="input text-sm">
              <option value="">All categories</option>
              {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="label text-xs">Location</label>
            <input type="text" placeholder="City or venue…" value={location}
              onChange={e => setLocation(e.target.value)} className="input text-sm" />
          </div>
          <div>
            <label className="label text-xs">From</label>
            <input type="datetime-local" value={startDate}
              onChange={e => setStartDate(e.target.value)} className="input text-sm" />
          </div>
          <div>
            <label className="label text-xs">To</label>
            <input type="datetime-local" value={endDate}
              onChange={e => setEndDate(e.target.value)} className="input text-sm" />
          </div>

          <div className="sm:col-span-2 lg:col-span-4 flex justify-end gap-2">
            {hasFilters && (
              <button onClick={clearAll} className="btn-ghost text-sm flex items-center gap-1">
                <X className="w-3 h-3" /> Clear
              </button>
            )}
            <button onClick={applyFilter} className="btn-primary text-sm">Apply Filters</button>
          </div>
        </div>
      )}
    </div>
  );
}
